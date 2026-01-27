package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.music.StructurePlaylist;
import com.github.charlyb01.timm.network.PlayPayload;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow @NotNull public abstract ServerLevel level();

    @Unique private Identifier currentSoundId;
    @Unique private final int tickCheck;

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
        this.tickCheck = this.getUUID().hashCode() % 20;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.isCreative()) return;
        if (this.tickCount % 20 != this.tickCheck) return;

        StructureManager structureManager = this.level().structureManager();
        BlockPos playerPos = this.blockPosition();
        HashMap<SectionPos, Set<Structure>> structuresByPos = timm_neoforge$getStructuresAroundPlayer(playerPos, structureManager);

        for (Map.Entry<SectionPos, Set<Structure>> entry : structuresByPos.entrySet()) {
            for (Structure struct : entry.getValue()) {
                var tagKeyOptional = struct.biomes().unwrapKey();
                if (tagKeyOptional.isEmpty()) continue;

                String structureName = timm_neoforge$getStructureName(tagKeyOptional.get());
                Integer distance = StructurePlaylist.DISTANCE_FROM_STRUCTURE.get(structureName);

                if (distance == null) {
                    Timm.debugLog("Structure distance was not registered for: " + structureName);
                    continue;
                }

                if (!timm_neoforge$structureContains(entry.getKey(), playerPos, struct, distance, structureManager)) continue;

                Identifier soundId = StructurePlaylist.EVENT_ID_FROM_STRUCTURE.get(structureName);
                if (soundId == null) {
                    Timm.debugLog("Structure ids were not registered for: " + structureName);
                    continue;
                }

                if (soundId.equals(this.currentSoundId)) break;
                this.currentSoundId = soundId;

                PacketDistributor.sendToPlayer((ServerPlayer) (Object) this, new PlayPayload(soundId));
                break;
            }
        }
    }

    @Unique
    @NotNull
    private static HashMap<SectionPos, Set<Structure>> timm_neoforge$getStructuresAroundPlayer(
            BlockPos playerPos,
            StructureManager structureManager
    ) {
        HashMap<SectionPos, Set<Structure>> structures = new HashMap<>();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                BlockPos pos = playerPos.offset(16 * i, 0, 16 * j);
                structures.put(SectionPos.of(pos), structureManager.getAllStructuresAt(pos).keySet());
            }
        }
        return structures;
    }

    @Unique
    private static boolean timm_neoforge$structureContains(
            SectionPos sectionPos,
            BlockPos playerPos,
            Structure structure,
            int expansion,
            StructureManager structureManager
    ) {
        for (StructureStart structureStart : structureManager.startsForStructure(sectionPos, structure)) {
            for (StructurePiece structurePiece : structureStart.getPieces()) {
                if (structurePiece.getBoundingBox().inflatedBy(expansion).isInside(playerPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static String timm_neoforge$getStructureName(TagKey<Biome> biomeTagKey) {
        var id = biomeTagKey.location().getPath().split("/");
        return id[id.length - 1];
    }
}