package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.config.StructureFadeOut;
import com.github.charlyb01.timm.imixin.MusicManagerIMixin;
import com.github.charlyb01.timm.imixin.VolumeSettingIMixin;
import com.github.charlyb01.timm.music.BiomePlaylist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
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

    @Shadow
    public abstract void startPlaying(Music type);

    @Unique private ResourceLocation timm$lastBiomeEvent;
    @Unique private ResourceLocation timm$structureEvent;
    @Unique private ResourceLocation timm$structureEventPlaying;
    @Unique private float timm$volume = 1.0F;
    @Unique private int timm$switchDelay = 0;
    @Unique private int timm$currentTrackTicks = 0;
    @Unique private @Nullable ResourceLocation timm$pendingBiomeSwitchTarget;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.minecraft.level == null || this.minecraft.player == null) return;

        if (this.currentMusic == null) {
            this.timm$currentTrackTicks = 0;
            if (this.timm$structureEvent != null) this.timm$playStructureMusic();
            return;
        }

        this.timm$currentTrackTicks++;

        float delta = 1.0F / (Config.FADE_DURATION.get() * 20);

        if (this.timm$shouldFadeOut()) {
            this.timm$volume = Math.max(0.0F, this.timm$volume - delta);
            ((VolumeSettingIMixin) this.minecraft.getSoundManager()).timm$setVolume(this.currentMusic, this.timm$volume);

            if (this.timm$volume > 0.f) return;
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.timm$volume = 1.f;
            this.nextSongDelay = Config.RESET_DELAY_ON_BIOME_SWITCH.get()
                ? this.random.nextIntBetweenInclusive(Config.MIN_DELAY.get(), Config.MAX_DELAY.get()) * 20
                : 10;
            this.currentMusic = null;
            this.timm$currentTrackTicks = 0;
            this.timm$resetBiomeSwitchState();

            if (this.timm$structureEvent == null) return;
            this.timm$playStructureMusic();
        } else if (this.timm$volume < 1.0F) {
            this.timm$volume = Math.min(1.0F, this.timm$volume + delta);
            ((VolumeSettingIMixin) this.minecraft.getSoundManager()).timm$setVolume(this.currentMusic, this.timm$volume);
        }
    }

    @Inject(method = "startPlaying", at = @At("HEAD"))
    private void saveCurrentBiome(CallbackInfo ci) {
        this.timm$lastBiomeEvent = BiomePlaylist.CURRENT_BIOME_EVENT;
        this.timm$currentTrackTicks = 0;
        this.timm$resetBiomeSwitchState();
        this.timm$volume = 1.0F;
    }

    @Inject(method = "startPlaying", at = @At("HEAD"))
    private void resetStructure(Music type, CallbackInfo ci) {
        this.timm$structureEventPlaying = null;
    }

    @Unique
    private @Nullable ResourceLocation timm$getBiomeSwitchTarget() {
        if (BiomePlaylist.UNDEFINED_BIOME.equals(this.timm$lastBiomeEvent)) {
            return null;
        }
        Optional<ResourceKey<Biome>> biomeKey = this.minecraft.level.getBiome(this.minecraft.player.blockPosition()).unwrapKey();
        if (biomeKey.isEmpty()) {
            Timm.debugLog("Biome was not registered: likely a bug!");
            return BiomePlaylist.UNDEFINED_BIOME;
        }

        ResourceLocation biomeId = biomeKey.get().location();
        var eventsForCurrentBiome = BiomePlaylist.EVENTS_BY_BIOME.get(biomeId);
        if (eventsForCurrentBiome == null) {
            Timm.debugLog("Current biome not registered in playlist: fade out to default");
            return biomeId;
        }

        return eventsForCurrentBiome.contains(this.timm$lastBiomeEvent) ? null : biomeId;
    }

    @Unique
    private void timm$resetBiomeSwitchState() {
        this.timm$pendingBiomeSwitchTarget = null;
        this.timm$switchDelay = 0;
    }

    @Unique
    boolean timm$shouldFadeOut() {
        if (this.timm$structureEvent != null && !this.timm$structureEvent.equals(this.timm$structureEventPlaying)) {
            return true;
        }

        if (this.timm$structureEventPlaying != null && Config.STRUCTURE_FADE_OUT.get().equals(StructureFadeOut.NEVER)) {
            return false;
        }

        ResourceLocation biomeSwitchTarget = this.timm$getBiomeSwitchTarget();
        if (biomeSwitchTarget == null) {
            this.timm$resetBiomeSwitchState();
            return false;
        }

        if (biomeSwitchTarget.equals(this.timm$pendingBiomeSwitchTarget)) {
            this.timm$switchDelay++;
        } else {
            this.timm$pendingBiomeSwitchTarget = biomeSwitchTarget;
            this.timm$switchDelay = 1;
        }

        if (this.timm$currentTrackTicks < Config.MIN_PLAY_TIME_BEFORE_BIOME_SWITCH.get() * 20) {
            return false;
        }

        return this.timm$switchDelay >= Config.FADE_DELAY.get() * 20;
    }

    @Unique
    private void timm$playStructureMusic() {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(this.timm$structureEvent.getNamespace(), this.timm$structureEvent.getPath());

        ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, rl);
        Optional<Holder.Reference<SoundEvent>> optHolder = BuiltInRegistries.SOUND_EVENT.getHolder(key);

        if (optHolder.isEmpty()) return;

        Music music = new Music(
                optHolder.get(),
                Config.MIN_DELAY.get() * 20,
                Config.MAX_DELAY.get() * 20,
                false
        );

        this.startPlaying(music);
        this.timm$structureEventPlaying = this.timm$structureEvent;
        this.timm$structureEvent = null;
    }

    @Override
    public void timm$setStructureEventId(ResourceLocation soundId) {
        this.timm$structureEvent = soundId;
    }

    @Override
    public void timm$clearStructureState() {
        this.timm$structureEvent = null;
        this.timm$structureEventPlaying = null;
        this.timm$resetBiomeSwitchState();
    }
}
