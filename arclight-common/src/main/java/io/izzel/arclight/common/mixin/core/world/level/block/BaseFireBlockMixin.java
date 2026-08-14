package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

    // fireExtinguished implemented per class

    // 26.1: ignite is deferred via InsideBlockEffectType.FIRE_IGNITE → fireIgnite(Entity)
    @Unique
    private static final ThreadLocal<BlockPos> arclight$fireCombustPos = new ThreadLocal<>();

    @Decorate(method = "entityInside", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/InsideBlockEffectApplier;apply(Lnet/minecraft/world/entity/InsideBlockEffectType;)V"))
    private void arclight$captureFireCombust(InsideBlockEffectApplier applier, InsideBlockEffectType type,
                                             BlockState state, Level level, BlockPos pos, Entity entity,
                                             InsideBlockEffectApplier applierArg, boolean bl) throws Throwable {
        arclight$fireCombustPos.set(pos.immutable());
        DecorationOps.callsite().invoke(applier, type);
    }

    @Decorate(method = "fireIgnite", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private static void arclight$onFire(Entity instance, float f) throws Throwable {
        BlockPos pos = arclight$fireCombustPos.get();
        try {
            if (pos != null) {
                var event = new EntityCombustByBlockEvent(
                    CraftBlock.at(instance.level(), pos),
                    ((EntityBridge) instance).bridge$getBukkitEntity(),
                    f);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                f = event.getDuration();
            }
            DecorationOps.callsite().invoke(instance, f);
        } finally {
            arclight$fireCombustPos.remove();
        }
    }

    @Redirect(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    public boolean arclight$extinguish2(Level world, BlockPos pos, boolean isMoving) {
        if (!CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.AIR.defaultBlockState()).isCancelled()) {
            world.removeBlock(pos, isMoving);
        }
        return false;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    private static boolean inPortalDimension(Level level) {
        var typeKey = ((WorldBridge) level).bridge$getTypeKey();
        return typeKey == LevelStem.NETHER || typeKey == LevelStem.OVERWORLD;
    }
}
