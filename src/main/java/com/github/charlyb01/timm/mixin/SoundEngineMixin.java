package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.imixin.VolumeSettingIMixin;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements VolumeSettingIMixin {
    @Shadow private boolean loaded;
    @Shadow @Final private Options options;
    @Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    @Final
    public SoundManager soundManager;

    @Override
    public void timm$setVolume(SoundInstance sound, float volume) {
        if (this.loaded) {
            ChannelAccess.ChannelHandle sourceManager = this.instanceToChannel.get(sound);
            if (sourceManager != null) {
                sourceManager.execute(source -> source.setVolume(volume * this.timm$calculateVolume(sound)));
            }
        }
    }

    @Unique
    private float timm$calculateVolume(SoundInstance sound) {
        return this.timm$calculateVolume(sound.getVolume(), sound.getSource());
    }

    @Unique
    private float timm$calculateVolume(float volume, SoundSource source) {
        return Mth.clamp(volume * this.timm$getSoundSourceVolume(source), 0.0F, 1.0F);
    }

    @Unique
    private float timm$getSoundSourceVolume(@Nullable SoundSource source) {
        return source != null && source != SoundSource.MASTER
                ? this.options.getSoundSourceVolume(source)
                : 1.0F;
    }
}
