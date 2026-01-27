package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.config.StructureFadeOut;
import com.github.charlyb01.timm.imixin.MusicManagerIMixin;
import com.github.charlyb01.timm.music.BiomePlaylist;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin implements MusicManagerIMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private RandomSource random;
    @Shadow private @Nullable SoundInstance currentMusic;
    @Shadow private int nextSongDelay;

    @Shadow public abstract void startPlaying(Music music);

    @Unique private Identifier timm_neoforge$lastBiomeEvent;
    @Unique private Identifier timm_neoforge$structureEvent;
    @Unique private Identifier timm_neoforge$structureEventPlaying;
    @Unique private float timm_neoforge$volume = 1.0F;
    @Unique private int timm_neoforge$switchDelay = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.minecraft.level == null || this.minecraft.player == null) return;

        if (this.currentMusic == null) {
            if (this.timm_neoforge$structureEvent != null) this.timm_neoforge$playStructureMusic();
            return;
        }

        // current is not null: fading management
        float delta = 1.f / (Config.FADE_DURATION.get() * 20);

        if (this.timm_neoforge$shouldFadeOut()) {
            this.timm_neoforge$volume = Math.max(0.f, this.timm_neoforge$volume - delta);
            this.minecraft.getSoundManager().updateCategoryVolume(SoundSource.MUSIC, this.timm_neoforge$volume);

            if (this.timm_neoforge$volume > 0.f) return;
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.timm_neoforge$volume = 1.f;
            this.minecraft.getSoundManager().updateCategoryVolume(SoundSource.MUSIC, this.timm_neoforge$volume);
            this.nextSongDelay = Config.RESET_DELAY_ON_BIOME_SWITCH.get()
                    ? this.random.nextInt(Config.MIN_DELAY.get(), Config.MAX_DELAY.get())
                    : 10;
            this.currentMusic = null;

            if (this.timm_neoforge$structureEvent == null) return;
            this.timm_neoforge$playStructureMusic();
        } else if (this.timm_neoforge$volume < 1.f) {
            this.timm_neoforge$volume = Math.min(1.f, this.timm_neoforge$volume + delta);
            this.minecraft.getSoundManager().updateCategoryVolume(SoundSource.MUSIC, this.timm_neoforge$volume);
        }
    }

    @Inject(method = "startPlaying", at = @At("HEAD"))
    private void saveCurrentBiome(CallbackInfo ci) {
        this.timm_neoforge$lastBiomeEvent = BiomePlaylist.CURRENT_BIOME_EVENT;
    }

    @Inject(method = "startPlaying", at = @At("HEAD"))
    private void resetStructure(CallbackInfo ci) {
        this.timm_neoforge$structureEventPlaying = null;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/MusicManager;fadePlaying(F)Z"))
    private boolean useOnlyOneFadeMethod(MusicManager instance, float volume, Operation<Boolean> original) {
        return false;
    }

    @Unique
    private boolean timm_neoforge$biomeSwitch() {
        if (BiomePlaylist.UNDEFINED_BIOME.equals(this.timm_neoforge$lastBiomeEvent)) {
            // This happens if we're opening a world in creative
            return false;
        }

        Holder<Biome> biomeHolder = this.minecraft.level.getBiome(this.minecraft.player.blockPosition());
        Optional<ResourceKey<Biome>> currentBiome = biomeHolder.unwrapKey();
        if (currentBiome.isEmpty()) {
            Timm.debugLog("Biome was not registered: likely a bug!");
            return true;
        }

        var eventsForCurrentBiome = BiomePlaylist.EVENTS_BY_BIOME.get(currentBiome.get().identifier());
        if (eventsForCurrentBiome == null) {
            Timm.debugLog("Current biome was not registered in playlist: fade out to default");
            return true;
        }

        return !eventsForCurrentBiome.contains(this.timm_neoforge$lastBiomeEvent);
    }

    @Unique
    private boolean timm_neoforge$shouldFadeOut() {
        if (this.timm_neoforge$structureEvent != null && !this.timm_neoforge$structureEvent.equals(this.timm_neoforge$structureEventPlaying)) return true;
        if (this.timm_neoforge$structureEventPlaying != null && Config.STRUCTURE_FADE_OUT.get().equals(StructureFadeOut.NEVER))
            return false;

        if (this.timm_neoforge$biomeSwitch()) {
            return ++this.timm_neoforge$switchDelay >= Config.FADE_DELAY.get() * 20;
        } else {
            this.timm_neoforge$switchDelay = 0;
            return false;
        }
    }

    @Unique
    private void timm_neoforge$playStructureMusic() {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(this.timm_neoforge$structureEvent);
        Music musicSound = new Music(
                Holder.direct(soundEvent),
                Config.MIN_DELAY.get(),
                Config.MAX_DELAY.get(),
                false
        );

        this.startPlaying(musicSound);
        this.timm_neoforge$structureEventPlaying = this.timm_neoforge$structureEvent;
        this.timm_neoforge$structureEvent = null;
    }

    @Override
    public void timm$setStructureEventId(Identifier soundId) {
        this.timm_neoforge$structureEvent = soundId;
    }
}