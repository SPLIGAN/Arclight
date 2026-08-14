package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.mod.server.block.CauldronHooks;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Eject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayeredCauldronBlock.class)
public class LayeredCauldronBlockMixin {

    // 26.1: extinguish/lower-fill moved into lambda$entityInside$0 via InsideBlockEffectApplier.
    @Inject(method = "lambda$entityInside$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LayeredCauldronBlock;handleEntityOnFireInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void arclight$extinguishReason(ServerLevel serverLevel, BlockPos pos, BlockState state, Level level, Entity entity, CallbackInfo ci) {
        CauldronHooks.setChangeReason(entity, CauldronLevelChangeEvent.ChangeReason.EXTINGUISH);
    }

    @Decorate(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/InsideBlockEffectApplier;apply(Lnet/minecraft/world/entity/InsideBlockEffectType;)V"))
    private void arclight$extinguishApply(InsideBlockEffectApplier applier, InsideBlockEffectType type) throws Throwable {
        try {
            if (type == InsideBlockEffectType.EXTINGUISH && !CauldronHooks.getResult()) {
                return;
            }
            DecorationOps.callsite().invoke(applier, type);
        } finally {
            CauldronHooks.reset();
        }
    }

    @Redirect(method = "lowerFillLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean arclight$lowerFill(Level level, BlockPos pos, BlockState state, BlockState old) {
        return CauldronHooks.changeLevel(old, level, pos, state, CauldronHooks.getEntity(), CauldronHooks.getReason());
    }

    @Redirect(method = "handlePrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean arclight$precipitation(Level level, BlockPos pos, BlockState state, BlockState old) {
        return CauldronHooks.changeLevel(old, level, pos, state, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL);
    }

    @Eject(method = "receiveStalactiteDrip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean arclight$drip(Level level, BlockPos pos, BlockState state, CallbackInfo ci, BlockState old) {
        if (CauldronHooks.changeLevel(old, level, pos, state, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL)) {
            return true;
        } else {
            ci.cancel();
            return false;
        }
    }
}
