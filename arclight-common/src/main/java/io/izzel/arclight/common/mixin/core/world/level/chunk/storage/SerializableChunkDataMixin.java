package io.izzel.arclight.common.mixin.core.world.level.chunk.storage;

import io.izzel.arclight.common.bridge.core.world.chunk.ChunkAccessBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public class SerializableChunkDataMixin {

    // Carried across the 26.1 parse/read and copyOf/write split; CompoundTag is no longer
    // a parameter of read()/write(), so @Local LVT capture is unavailable and unsafe here.
    @Unique
    private CompoundTag arclight$bukkitValues;

    @Inject(method = "parse", at = @At("RETURN"))
    private static void arclight$captureBukkitValues(LevelHeightAccessor heightAccessor, PalettedContainerFactory factory, CompoundTag tag, CallbackInfoReturnable<SerializableChunkData> cir) {
        SerializableChunkData data = cir.getReturnValue();
        if (data == null) {
            return;
        }
        Tag persistentBase = tag.get("ChunkBukkitValues");
        if (persistentBase instanceof CompoundTag compoundTag) {
            ((SerializableChunkDataMixin) (Object) data).arclight$bukkitValues = compoundTag;
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void arclight$loadPersistent(ServerLevel level, PoiManager poiManager, RegionStorageInfo storageInfo, ChunkPos chunkPos, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk chunk = cir.getReturnValue();
        if (chunk == null || this.arclight$bukkitValues == null) {
            return;
        }
        ((CraftPersistentDataContainer) ((ChunkAccessBridge) chunk).bridge$getPersistentDataContainer()).putAll(this.arclight$bukkitValues);
    }

    @Inject(method = "copyOf", at = @At("RETURN"))
    private static void arclight$copyBukkitValues(ServerLevel level, ChunkAccess chunkAccess, CallbackInfoReturnable<SerializableChunkData> cir) {
        SerializableChunkData data = cir.getReturnValue();
        if (data == null) {
            return;
        }
        var container = (CraftPersistentDataContainer) ((ChunkAccessBridge) chunkAccess).bridge$getPersistentDataContainer();
        if (!container.isEmpty()) {
            ((SerializableChunkDataMixin) (Object) data).arclight$bukkitValues = container.toTagCompound();
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void arclight$savePersistent(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (tag != null && this.arclight$bukkitValues != null && !this.arclight$bukkitValues.isEmpty()) {
            tag.put("ChunkBukkitValues", this.arclight$bukkitValues);
        }
    }
}
