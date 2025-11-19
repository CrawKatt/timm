package com.github.charlyb01.timm.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayPacket(ResourceLocation soundId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayPacket> TYPE =
            new CustomPacketPayload.Type<>(Constants.PLAY_PACKET_ID);

    public static final StreamCodec<ByteBuf, PlayPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, // codec para ResourceLocation
                    PlayPacket::soundId,
                    PlayPacket::new
            );

    public PlayPacket(final FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.soundId());
    }
}
