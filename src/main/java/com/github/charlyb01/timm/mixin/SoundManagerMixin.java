package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.command.NowPlayingCmd;
import com.github.charlyb01.timm.imixin.VolumeSettingIMixin;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin implements VolumeSettingIMixin {
    @Shadow @Final private SoundEngine soundEngine;

    @Inject(method = "play", at = @At("TAIL"))
    private void saveMusicIdentifier(SoundInstance sound, CallbackInfo ci) {
        if (!sound.getSource().equals(SoundSource.MUSIC) || sound.getSound() == null) return;
        NowPlayingCmd.SONG_ID = sound.getSound().getLocation();
    }

    @Inject(method = "stop*", at = @At("HEAD"))
    private void resetMusicIdentifierOnStop(CallbackInfo ci) {
        NowPlayingCmd.SONG_ID = null;
    }

    @Override
    public void timm$setVolume(SoundInstance sound, float volume) {
        ((VolumeSettingIMixin) this.soundEngine).timm$setVolume(sound, volume);
    }
}
