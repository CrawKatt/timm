package com.github.charlyb01.timm.music;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class StructurePlaylist {
    public static HashMap<String, Integer> DISTANCE_FROM_STRUCTURE = new HashMap<>();
    public static HashMap<String, Identifier> EVENT_ID_FROM_STRUCTURE = new HashMap<>();

    public static void init() {
        Timm.LOGGER.info("Initializing structure playlists");

        InputStream stream = getInputStream();
        if (stream == null) return;

        try {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(stream));
            while (jsonReader.hasNext()) {
                JsonToken jsonToken = jsonReader.peek();
                if (jsonToken == JsonToken.BEGIN_OBJECT) {
                    jsonReader.beginObject();
                } else if (jsonToken == JsonToken.END_OBJECT) {
                    jsonReader.endObject();
                } else {
                    String structure = jsonReader.nextName();
                    Identifier structureId = Timm.id(structure);
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
                            } else {
                                jsonReader.skipValue();
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
            jsonReader.close();
            Timm.LOGGER.info("Structure playlists successfully initialized");
        } catch (IOException e) {
            Timm.LOGGER.error("Error reading structure playlist file: {}", e.getMessage());
        }
    }

    private static InputStream getInputStream() {
        Path loader = FMLPaths.CONFIGDIR.get();
        Path filePath = loader
                .resolve(Timm.MOD_ID)
                .resolve("structure_playlists.json");

        if (Files.exists(filePath)) {
            try {
                return Files.newInputStream(filePath);
            } catch (IOException e) {
                Timm.LOGGER.error("Error opening external structure_playlists.json", e);
                return null;
            }
        }

        if (Config.DEBUG_LOG.get()) {
            Timm.LOGGER.info("Player structure_playlists.json not found, using default one");
        }

        Optional<? extends ModContainer> container = ModList.get().getModContainerById(Timm.MOD_ID);
        if (container.isEmpty()) {
            Timm.LOGGER.error("Mod not correctly loaded");
            return null;
        }

        ModContainer mod = container.get();
        IModInfo modInfo = mod.getModInfo();

        try {
            InputStream internalStream = modInfo.getOwningFile()
                    .getFile()
                    .getContents()
                    .openFile("assets/timm/custom/structure_playlists.json");

            if (internalStream == null) {
                Timm.LOGGER.error("Default structure_playlists.json does not exist in JarContents");
                return null;
            }
            return internalStream;
        } catch (IOException e) {
            Timm.LOGGER.error("Failed to read internal structure_playlists.json", e);
            return null;
        }
    }
}