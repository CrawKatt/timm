package com.github.charlyb01.timm.network;

import com.github.charlyb01.timm.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkingRegistry {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                PlayPacket.TYPE,
                PlayPacket.STREAM_CODEC,
                ((payload, context) -> handlePlayOnMain(payload))
        );
    }

    private static void handlePlayOnMain(final PlayPacket packet) {
        if (Config.ENABLE_STRUCTURE_MUSIC.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        MusicManager musicManager = mc.getMusicManager();
        musicManager.stopPlaying();

        ResourceLocation soundId = packet.soundId();
        SoundEvent event = SoundEvent.createVariableRangeEvent(soundId);
        Level level = mc.level;
        level.playSound(
                mc.player,
                mc.player.blockPosition(),
                event,
                SoundSource.MUSIC,
                1.0F,
                1.0F
        );
    }
}
