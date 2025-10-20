package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.imixin.VolumeSettingIMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private @Nullable SoundInstance currentMusic;
    @Shadow private int nextSongDelay;

    @Shadow
    public abstract void stopPlaying();

    @Unique private Holder<Biome> biome;
    @Unique private float volume = 1.0F;

    @Inject(method = "tick", at = @At("HEAD"))
    private void fadeOutMusic(CallbackInfo ci) {
        if (this.minecraft.level == null || this.minecraft.player == null || this.biome == null) return;
        if (this.biome.unwrapKey().isEmpty()) {
            Timm.debugLog("Empty registry key for biome! Likely a bug");
            return;
        }

        float delta = 1.0F / (Config.FADE_DURATION.get() * 20);
        boolean fadeIn = this.minecraft.level
                .getBiome(this.minecraft.player.blockPosition())
                .is(this.biome.unwrapKey().get());

        if (fadeIn) {
            if (this.volume < 1.0F) {
                this.volume = Math.min(1.0F, this.volume + delta);
                ((VolumeSettingIMixin) this.minecraft.getSoundManager())
                        .timm$setVolume(this.currentMusic, this.volume);
            }
        } else {
            this.volume = Math.max(0.0F, this.volume - delta);
            ((VolumeSettingIMixin) this.minecraft.getSoundManager())
                    .timm$setVolume(this.currentMusic, this.volume);

            if (this.volume == 0.0F) {
                this.stopPlaying();
                this.volume = 1.0F;
                this.nextSongDelay = 10;
                this.currentMusic = null;
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/MusicManager;startPlaying(Lnet/minecraft/sounds/Music;)V"))
    private void saveCurrentBiome(CallbackInfo ci) {
        if (this.minecraft.level == null || this.minecraft.player == null) return;

        this.biome = this.minecraft.level.getBiome(this.minecraft.player.blockPosition());
    }
}
