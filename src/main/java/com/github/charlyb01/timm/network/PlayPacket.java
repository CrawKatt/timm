package com.github.charlyb01.timm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;


public record PlayPacket(ResourceLocation soundId) {
    public static PlayPacket decode(FriendlyByteBuf buf) {
        return new PlayPacket(buf.readResourceLocation());
    }

    public static void encode(PlayPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.soundId());
    }

    public static void handle(PlayPacket packet, CustomPayloadEvent.Context ctx) {
        NetworkingRegistry.handlePlayOnMain(packet);
    }
}
