package com.github.charlyb01.timm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayPacket(ResourceLocation soundId) {

    public PlayPacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(soundId);
    }

    public static void handle(PlayPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> NetworkingRegistry.handlePlayOnMain(packet));
        ctx.get().setPacketHandled(true);
    }
}
