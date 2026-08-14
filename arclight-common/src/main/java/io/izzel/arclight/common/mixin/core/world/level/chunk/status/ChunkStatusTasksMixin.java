package io.izzel.arclight.common.mixin.core.world.level.chunk.status;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {

    // 26.1: structure flag comes from WorldOptions (via MinecraftServer.getWorldGenSettings), not WorldData.
    // Prefer Bukkit world's generateStructures setting when available.
    @Decorate(method = "generateStructureStarts", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldOptions;generateStructures()Z"))
    private static boolean arclight$useLevelStructures(WorldOptions options, @Local(ordinal = -1) ServerLevel level) throws Throwable {
        if (((WorldBridge) level).bridge$getWorld() != null) {
            return ((WorldBridge) level).bridge$getWorld().canGenerateStructures();
        }
        return (boolean) DecorationOps.callsite().invoke(options);
    }
}
