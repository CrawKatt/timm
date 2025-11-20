package com.github.charlyb01.timm.network;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.imixin.MusicManagerIMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkingRegistry {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Timm.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(
                id++,
                PlayPacket.class,
                PlayPacket::write,
                PlayPacket::new,
                PlayPacket::handle
        );
    }

    public static void handlePlayOnMain(final PlayPacket packet) {
        if (Config.ENABLE_STRUCTURE_MUSIC.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        MusicManager musicManager = mc.getMusicManager();
        musicManager.stopPlaying();

        ResourceLocation soundId = packet.soundId();
        ((MusicManagerIMixin) musicManager).timm$setStructureEventId(soundId);
    }
}
