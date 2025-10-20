package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.imixin.VolumeSettingIMixin;
import com.github.charlyb01.timm.music.BiomePlaylist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private @Nullable SoundInstance currentMusic;
    @Shadow private int nextSongDelay;

    @Shadow
    public abstract void stopPlaying();

    @Unique private ResourceLocation timm$lastBiomeEvent;
    @Unique private float timm$volume = 1.0F;

    @Inject(method = "tick", at = @At("HEAD"))
    private void fadeOutMusic(CallbackInfo ci) {
        if (this.currentMusic == null || this.minecraft.level == null || this.minecraft.player == null) return;

        float delta = 1.0F / (Config.FADE_DURATION.get() * 20);

        if (this.timm$shouldFadeOut()) {
            this.timm$volume = Math.max(0.0F, this.timm$volume - delta);
            ((VolumeSettingIMixin) this.minecraft.getSoundManager()).timm$setVolume(this.currentMusic, this.timm$volume);

            if (this.timm$volume == 0.0F) {
                this.stopPlaying();
                this.timm$volume = 1.0F;
                this.nextSongDelay = 10;
                this.currentMusic = null;
            }
        } else if (this.timm$volume < 1.0F) {
            this.timm$volume = Math.min(1.0F, this.timm$volume + delta);
            ((VolumeSettingIMixin) this.minecraft.getSoundManager()).timm$setVolume(this.currentMusic, this.timm$volume);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/MusicManager;startPlaying(Lnet/minecraft/sounds/Music;)V"))
    private void saveCurrentBiome(CallbackInfo ci) {
        this.timm$lastBiomeEvent = BiomePlaylist.CURRENT_BIOME_EVENT;
    }

    @Unique
    private boolean timm$shouldFadeOut() {
        Optional<ResourceKey<Biome>> biomeKey = this.minecraft.level.getBiome(this.minecraft.player.blockPosition()).unwrapKey();
        if (biomeKey.isEmpty()) {
            Timm.debugLog("Biome was not registered: likely a bug!");
            return true;
        }

        ArrayList<ResourceLocation> eventsForCurrentBiome = BiomePlaylist.EVENTS_BY_BIOME.get(biomeKey.get().location());
        if (eventsForCurrentBiome == null) {
            Timm.debugLog("Current biome not registered in playlist: fade out to default");
            return true;
        }

        return !eventsForCurrentBiome.contains(this.timm$lastBiomeEvent);
    }
}
