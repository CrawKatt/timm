package com.github.charlyb01.timm.network;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.imixin.MusicManagerIMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public final class NetworkingRegistry {

    public static final ResourceLocation CHANNEL_NAME = Timm.id("main");

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(CHANNEL_NAME)
            .networkProtocolVersion(1)
            .clientAcceptedVersions((v, i) -> true)
            .serverAcceptedVersions((v, i) -> true)
            .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(PlayPacket.class)
                .consumerMainThread(PlayPacket::handle)
                .add();
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
