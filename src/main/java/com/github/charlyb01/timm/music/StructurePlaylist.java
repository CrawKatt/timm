package com.github.charlyb01.timm.music;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class StructurePlaylist {
    public static final HashMap<String, Integer> DISTANCE_FROM_STRUCTURE = new HashMap<>();
    public static final HashMap<String, ResourceLocation> EVENT_ID_FROM_STRUCTURE = new HashMap<>();

    public static void init() {
        Timm.LOGGER.info("Initializing structure playlists");

        Path path = getPath();
        if (path == null) return;

        DISTANCE_FROM_STRUCTURE.clear();
        EVENT_ID_FROM_STRUCTURE.clear();

        try {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(Files.newInputStream(path)));
            while (jsonReader.hasNext()) {
                JsonToken jsonToken = jsonReader.peek();
                if (jsonToken == JsonToken.BEGIN_OBJECT) {
                    jsonReader.beginObject();
                } else if (jsonToken == JsonToken.END_OBJECT) {
                    jsonReader.endObject();
                } else {
                    String structure = jsonReader.nextName();
                    ResourceLocation structureId = Timm.id(structure);
                    int distance = 0;
                    ArrayList<String> structures = new ArrayList<>();

                    if (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String name = jsonReader.nextName();
                            if (name.equals("distance")) {
                                distance = jsonReader.nextInt();
                            } else if (name.equals("structures") && jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
                                jsonReader.beginArray();
                                while (jsonReader.hasNext()) {
                                    String musicId = jsonReader.nextString();
                                    structures.add(musicId);
                                }
                                jsonReader.endArray();
                            }
                        }
                        jsonReader.endObject();
                    }

                    for (String structureName : structures) {
                        DISTANCE_FROM_STRUCTURE.put(structureName, distance);
                        EVENT_ID_FROM_STRUCTURE.put(structureName, structureId);
                    }
                }
            }
            Timm.LOGGER.info("Structure playlists successfully initialized");
        } catch (IOException e) {
            Timm.LOGGER.error("Error reading structure playlist file: {}", e.getMessage());
        }
    }

    public static Integer getDistance(ResourceLocation structureId) {
        Integer distance = DISTANCE_FROM_STRUCTURE.get(structureId.toString());
        return distance != null ? distance : DISTANCE_FROM_STRUCTURE.get(structureId.getPath());
    }

    public static ResourceLocation getEventId(ResourceLocation structureId) {
        ResourceLocation eventId = EVENT_ID_FROM_STRUCTURE.get(structureId.toString());
        return eventId != null ? eventId : EVENT_ID_FROM_STRUCTURE.get(structureId.getPath());
    }

    private static Path getPath() {
        Path loader = FMLPaths.CONFIGDIR.get();
        Path filePath = loader
                .resolve(Timm.MOD_ID)
                .resolve("structure_playlists.json");

        if (Files.exists(filePath)) {
            return filePath;
        }

        Timm.debugLog("Player structure_playlists.json not found, using default one");

        Optional<? extends ModContainer> container = ModList.get().getModContainerById(Timm.MOD_ID);
        if (container.isEmpty()) {
            Timm.LOGGER.error("Mod not correctly loaded");
            return null;
        }

        ModContainer mod = container.get();
        Optional<Path> path = Optional.of(mod
                .getModInfo()
                .getOwningFile()
                .getFile()
                .findResource("assets/timm/custom/structure_playlists.json")
        );

        if (path.isEmpty()) {
            Timm.LOGGER.error("Could not locate default structure_playlists.json");
            return null;
        }

        filePath = path.get();
        if (!Files.exists(filePath)) {
            Timm.LOGGER.error("Default structure_playlists.json does not exist");
            return null;
        }

        return filePath;
    }
}
