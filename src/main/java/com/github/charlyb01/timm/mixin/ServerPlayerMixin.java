package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.music.StructurePlaylist;
import com.github.charlyb01.timm.network.PlayPacket;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.tags.TagKey;
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
    @Shadow public abstract ServerLevel serverLevel();

    @Unique private ResourceLocation timm$currentSoundId;
    @Unique private final int timm$tickCheck;

    public ServerPlayerMixin(Level level, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(level, pos, yaw, gameProfile);
        this.timm$tickCheck = this.uuid.hashCode() % 20;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.tickCount % 20 != this.timm$tickCheck) return; // una vez por segundo, distribuido por jugador

        StructureManager structureManager = this.serverLevel().structureManager();
        BlockPos playerPos = this.blockPosition();
        HashMap<SectionPos, Set<Structure>> structuresByPos = timm$getStructuresAroundPlayer(playerPos, structureManager);

        for (Map.Entry<SectionPos, Set<Structure>> entry : structuresByPos.entrySet()) {
            for (Structure struct : entry.getValue()) {
                var tagKey = struct.biomes().unwrapKey();

                if (tagKey.isEmpty()) continue;

                String structureName = timm$getStructureName(tagKey.get());
                Integer distance = StructurePlaylist.DISTANCE_FROM_STRUCTURE.get(structureName);
                if (distance == null) {
                    Timm.debugLog("Structure distance was not registered for: " + structureName);
                    continue;
                }

                if (!timm$structureContains(entry.getKey(), playerPos, struct, distance, structureManager)) continue;

                ResourceLocation soundId = StructurePlaylist.EVENT_ID_FROM_STRUCTURE.get(structureName);
                if (soundId.equals(this.timm$currentSoundId)) break;

                this.timm$currentSoundId = soundId;
                PacketDistributor.sendToPlayer((ServerPlayer) (Object)this, new PlayPacket(soundId));

                break;
            }
        }
    }

    @Unique
    private static @NotNull HashMap<SectionPos, Set<Structure>> timm$getStructuresAroundPlayer(
            BlockPos playerPos, StructureManager structureManager) {
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
    private static boolean timm$structureContains(SectionPos sectionPos, BlockPos playerPos, Structure structure,
                                                  int expansion, StructureManager structureManager) {
        for (StructureStart start : structureManager.startsForStructure(sectionPos, structure)) {
            for (StructurePiece piece : start.getPieces()) {
                if (piece.getBoundingBox().inflatedBy(expansion).isInside(playerPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static String timm$getStructureName(TagKey<Biome> biomeTagKey) {
        var id = biomeTagKey.location().getPath().split("/");
        return id[id.length - 1];
    }
}
