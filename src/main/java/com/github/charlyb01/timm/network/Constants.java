package com.github.charlyb01.timm.network;

import com.github.charlyb01.timm.Timm;
import net.minecraft.resources.ResourceLocation;

public interface Constants {
    ResourceLocation PLAY_PACKET_ID = Timm.id("play_packet");
    ResourceLocation CLEAR_STRUCTURE_PACKET_ID = Timm.id("clear_structure_packet");
}
