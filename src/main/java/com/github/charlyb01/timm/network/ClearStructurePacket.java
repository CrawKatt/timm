package com.github.charlyb01.timm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClearStructurePacket() implements CustomPacketPayload {
    public static final ClearStructurePacket INSTANCE = new ClearStructurePacket();
    public static final CustomPacketPayload.Type<ClearStructurePacket> TYPE =
            new CustomPacketPayload.Type<>(Constants.CLEAR_STRUCTURE_PACKET_ID);

    public static final StreamCodec<FriendlyByteBuf, ClearStructurePacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
