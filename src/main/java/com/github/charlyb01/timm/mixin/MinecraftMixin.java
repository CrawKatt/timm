package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.music.BiomePlaylist;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    public LocalPlayer player;

    @ModifyExpressionValue(method = "getSituationalMusic", at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/Musics;MENU:Lnet/minecraft/sounds/Music;", opcode = Opcodes.GETSTATIC))
    private Music updateMenuMusic(Music original) {
        if (this.player == null) return original;

        Music musicSound = BiomePlaylist.getMenuMusic(this.player.getRandom());
        return musicSound != null ? musicSound : original;
    }

    @ModifyExpressionValue(
            method = "getSituationalMusic",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BackgroundMusic;select(ZZ)Ljava/util/Optional;")
    )
    private Optional<Music> updateBiomeMusic(Optional<Music> original) {
        if (this.player == null) return original;
        Level world = this.player.level();

        if (world.dimension() == Level.END) {
            Music music = BiomePlaylist.getEndMusic(this.player.getRandom());
            return music != null ? Optional.of(music) : original;
        }

        if (this.player.getAbilities().instabuild && this.player.getAbilities().mayfly) {
            Music music = BiomePlaylist.getCreativeMusic(this.player.getRandom());
            return music != null ? Optional.of(music) : original;
        }

        Holder<Biome> biome = world.getBiome(this.player.blockPosition());
        Optional<ResourceKey<Biome>> biomeKey = biome.unwrapKey();
        if (biomeKey.isEmpty()) return original;

        Music music = BiomePlaylist.getMusicSound(biomeKey.get().identifier(), this.player.getRandom());
        return music != null ? Optional.of(music) : original;
    }
}