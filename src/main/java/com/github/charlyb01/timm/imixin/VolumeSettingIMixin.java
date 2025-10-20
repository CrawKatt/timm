package com.github.charlyb01.timm.imixin;

import net.minecraft.client.resources.sounds.SoundInstance;

public interface VolumeSettingIMixin {
    void timm$setVolume(SoundInstance sound, float volume);
}
