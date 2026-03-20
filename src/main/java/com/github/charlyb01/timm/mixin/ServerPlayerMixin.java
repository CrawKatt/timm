package com.github.charlyb01.timm.mixin;

import com.github.charlyb01.timm.Timm;
import com.github.charlyb01.timm.network.ClearStructurePacket;
import com.github.charlyb01.timm.music.StructurePlaylist;
import com.github.charlyb01.timm.network.PlayPacket;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow public abstract ServerLevel serverLevel();

    @Unique private ResourceLocation timm$currentSoundId;
    @Unique private static final Set<String> timm$missingStructures = new HashSet<>();
    @Unique private final int timm$tickCheck;

    public ServerPlayerMixin(Level level, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(level, pos, yaw, gameProfile);
        this.timm$tickCheck = Math.floorMod(this.uuid.hashCode(), 20);
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.isCreative()) {
            this.timm$clearStructureMusic();
            return;
        }
        if (this.tickCount % 20 != this.timm$tickCheck) return;

        StructureManager structureManager = this.serverLevel().structureManager();
        BlockPos playerPos = this.blockPosition();
        ResourceLocation soundId = timm$getStructureSound(playerPos, structureManager);

        if (Objects.equals(soundId, this.timm$currentSoundId)) return;

        this.timm$currentSoundId = soundId;
        if (soundId != null) {
            PacketDistributor.sendToPlayer((ServerPlayer) (Object)this, new PlayPacket(soundId));
        } else {
            PacketDistributor.sendToPlayer((ServerPlayer) (Object)this, ClearStructurePacket.INSTANCE);
        }
    }

    @Unique
    private void timm$clearStructureMusic() {
        if (this.timm$currentSoundId == null) return;

        this.timm$currentSoundId = null;
        PacketDistributor.sendToPlayer((ServerPlayer) (Object)this, ClearStructurePacket.INSTANCE);
    }

    @Unique
    private static ResourceLocation timm$getStructureSound(BlockPos playerPos, StructureManager structureManager) {
        var structureRegistry = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceLocation soundId = null;
        int bestDistanceSq = Integer.MAX_VALUE;

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                BlockPos pos = playerPos.offset(16 * i, 0, 16 * j);
                SectionPos sectionPos = SectionPos.of(pos);
                ArrayList<Structure> structures = new ArrayList<>(structureManager.getAllStructuresAt(pos).keySet());
                structures.sort(Comparator.comparing(structure -> {
                    ResourceLocation structureId = structureRegistry.getKey(structure);
                    return structureId == null ? "" : structureId.toString();
                }));

                for (Structure structure : structures) {
                    ResourceLocation structureId = structureRegistry.getKey(structure);
                    if (structureId == null) continue;

                    Integer distance = StructurePlaylist.getDistance(structureId);
                    if (distance == null) {
                        timm$logMissingStructureDistance(structureId);
                        continue;
                    }

                    int distanceSq = timm$getDistanceSqToStructure(sectionPos, playerPos, structure, distance, structureManager);
                    if (distanceSq >= bestDistanceSq) continue;

                    ResourceLocation eventId = StructurePlaylist.getEventId(structureId);
                    if (eventId == null) continue;

                    bestDistanceSq = distanceSq;
                    soundId = eventId;
                }
            }
        }

        return soundId;
    }

    @Unique
    private static int timm$getDistanceSqToStructure(
            SectionPos sectionPos,
            BlockPos playerPos,
            Structure structure,
            int expansion,
            StructureManager structureManager
    ) {
        int bestDistanceSq = Integer.MAX_VALUE;

        for (StructureStart start : structureManager.startsForStructure(sectionPos, structure)) {
            for (StructurePiece piece : start.getPieces()) {
                BoundingBox boundingBox = piece.getBoundingBox();
                if (!boundingBox.inflatedBy(expansion).isInside(playerPos)) continue;

                bestDistanceSq = Math.min(bestDistanceSq, timm$getDistanceSq(playerPos, boundingBox));
                if (bestDistanceSq == 0) return 0;
            }
        }

        return bestDistanceSq;
    }

    @Unique
    private static int timm$getDistanceSq(BlockPos playerPos, BoundingBox boundingBox) {
        int x = timm$getAxisDistance(playerPos.getX(), boundingBox.minX(), boundingBox.maxX());
        int y = timm$getAxisDistance(playerPos.getY(), boundingBox.minY(), boundingBox.maxY());
        int z = timm$getAxisDistance(playerPos.getZ(), boundingBox.minZ(), boundingBox.maxZ());
        return x * x + y * y + z * z;
    }

    @Unique
    private static int timm$getAxisDistance(int value, int min, int max) {
        if (value < min) return min - value;
        return Math.max(0, value - max);
    }

    @Unique
    private static void timm$logMissingStructureDistance(@NotNull ResourceLocation structureId) {
        if (timm$missingStructures.add(structureId.toString())) {
            Timm.debugLog("Structure distance was not registered for: " + structureId);
        }
    }
}
