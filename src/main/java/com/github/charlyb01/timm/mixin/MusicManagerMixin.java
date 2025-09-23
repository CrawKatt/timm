package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.command.NowPlayingCmd;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Shadow @Nullable private SoundInstance currentMusic;

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/sounds/MusicManager;currentMusic:Lnet/minecraft/client/resources/sounds/SoundInstance;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void resetMusicIdentifierOnNull(CallbackInfo ci) {
        if (this.currentMusic == null || this.currentMusic.getSound() == null) return;

        if (this.currentMusic.getSound().getLocation().equals(NowPlayingCmd.SONG_ID)) {
            NowPlayingCmd.SONG_ID = null;
        }
    }

    @Inject(method = "stopPlaying()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
    private void resetMusicIdentifierOnStop(CallbackInfo ci) {
        NowPlayingCmd.SONG_ID = null;
    }
}