package com.github.charlyb01.timm.music;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.registry.SoundEventRegistry;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
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

public class BiomePlaylist {
    // Missing Fields Restored
    public static final Identifier UNDEFINED_BIOME = Timm.id("undefined_biome");
    public static Identifier CURRENT_BIOME_EVENT = UNDEFINED_BIOME;

    public static final HashMap<Identifier, ArrayList<Identifier>> EVENTS_BY_BIOME = new HashMap<>();

    private static final Identifier CREATIVE_ID = Identifier.tryParse("creative");
    private static final Identifier MENU_ID = Identifier.tryParse("menu");
    private static final Identifier END_ID = Identifier.tryParse("end");

    public static Music getMusicSound(Identifier biomeId, RandomSource random) {
        ArrayList<Identifier> musics = EVENTS_BY_BIOME.get(biomeId);
        if (musics == null || musics.isEmpty()) {
            // Update state: No music found for this biome
            CURRENT_BIOME_EVENT = UNDEFINED_BIOME;
            return null;
        }

        Identifier soundEventId = musics.get(random.nextInt(musics.size()));
        Holder<SoundEvent> soundEvent = SoundEventRegistry.SOUNDEVENT_BY_ID.get(soundEventId);

        if (soundEvent == null) {
            // Update state: Sound event not found in registry
            CURRENT_BIOME_EVENT = UNDEFINED_BIOME;
            return null;
        }

        // Update state: Success
        CURRENT_BIOME_EVENT = soundEventId;

        return new Music(
                soundEvent,
                Config.MIN_DELAY.get() * 20,
                Config.MAX_DELAY.get() * 20,
                false
        );
    }

    public static Music getCreativeMusic(RandomSource random) {
        ArrayList<Identifier> musics = EVENTS_BY_BIOME.get(CREATIVE_ID);
        if (musics == null || musics.isEmpty()) return null;

        Identifier soundEventId = musics.get(random.nextInt(musics.size()));
        Holder<SoundEvent> soundEvent = SoundEventRegistry.SOUNDEVENT_BY_ID.get(soundEventId);
        if (soundEvent == null) return null;

        return new Music(
                soundEvent,
                Config.MIN_DELAY.get() * 20,
                Config.MAX_DELAY.get() * 20,
                false
        );
    }

    public static Music getEndMusic(RandomSource random) {
        ArrayList<Identifier> musics = EVENTS_BY_BIOME.get(END_ID);
        if (musics == null || musics.isEmpty()) return null;

        Identifier soundEventId = musics.get(random.nextInt(musics.size()));
        Holder<SoundEvent> soundEvent = SoundEventRegistry.SOUNDEVENT_BY_ID.get(soundEventId);
        if (soundEvent == null) return null;

        return new Music(
                soundEvent,
                Config.MIN_DELAY.get() * 20,
                Config.MAX_DELAY.get() * 20,
                false
        );
    }

    public static Music getMenuMusic(RandomSource random) {
        ArrayList<Identifier> musics = EVENTS_BY_BIOME.get(MENU_ID);
        if (musics == null || musics.isEmpty()) return null;

        Identifier soundEventId = musics.get(random.nextInt(musics.size()));
        Holder<SoundEvent> soundEvent = SoundEventRegistry.SOUNDEVENT_BY_ID.get(soundEventId);
        if (soundEvent == null) return null;

        return new Music(soundEvent, 20, 60, false);
    }

    public static void init() {
        Timm.LOGGER.info("Initializing biome playlists");

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
                    String biomeName = jsonReader.nextName();
                    Identifier biomeId = Identifier.tryParse(biomeName);
                    ArrayList<Identifier> musics = new ArrayList<>();

                    if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            String musicId = jsonReader.nextString();
                            musics.add(Identifier.tryParse(musicId));
                        }
                        jsonReader.endArray();
                    }

                    EVENTS_BY_BIOME.put(biomeId, musics);
                }
            }
            jsonReader.close();
            Timm.LOGGER.info("Biome playlists successfully initialized");
        } catch (IOException why) {
            Timm.LOGGER.error("Error reading biome playlist file: {}", why.getMessage());
        }
    }

    private static InputStream getInputStream() {
        Path loader = FMLPaths.CONFIGDIR.get();
        Path filePath = loader
                .resolve(Timm.MOD_ID)
                .resolve("biome_playlists.json");

        if (Files.exists(filePath)) {
            try {
                return Files.newInputStream(filePath);
            } catch (IOException e) {
                Timm.LOGGER.error("Error opening external biome_playlists.json", e);
                return null;
            }
        }

        if (Config.DEBUG_LOG.get()) {
            Timm.LOGGER.info("Player biome_playlist.json not found, using default one");
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
                    .openFile("assets/timm/custom/biome_playlists.json");

            if (internalStream == null) {
                Timm.LOGGER.error("Default biome_playlists.json does not exist in JarContents");
                return null;
            }
            return internalStream;
        } catch (IOException e) {
            Timm.LOGGER.error("Failed to read internal biome_playlists.json", e);
            return null;
        }
    }
}