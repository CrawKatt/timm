package com.github.charlyb01.timm.network;

import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.imixin.MusicManagerIMixin;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleData(final PlayPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!Config.ENABLE_STRUCTURE_MUSIC.get()) return;
            Minecraft client = Minecraft.getInstance();

            if (client.level == null || client.player == null) return;
            if (client.getMusicManager() instanceof MusicManagerIMixin mixin) {
                mixin.timm$setStructureEventId(payload.soundId());
            }
        });
    }
}