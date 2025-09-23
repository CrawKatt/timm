package com.github.charlyb01.timm.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "playSound", at = @At("TAIL"))
    private void setCurrentSound(double x, double y, double z, SoundEvent event, SoundSource category, float volume,
                                 float pitch, boolean useDistance, long seed, CallbackInfo ci,
                                 @Local SimpleSoundInstance soundInstance) {
        if (category.equals(SoundSource.MUSIC) && !useDistance) {
            ((MusicManagerAccessor) this.minecraft.getMusicManager()).setCurrent(soundInstance);
        }
    }
}
