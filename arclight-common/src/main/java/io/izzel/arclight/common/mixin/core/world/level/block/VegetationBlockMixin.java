package io.izzel.arclight.common.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VegetationBlock.class)
public abstract class VegetationBlockMixin extends BlockMixin {

    // 26.1: plant break-on-update moved from BushBlock to VegetationBlock; updateShape signature changed.
    @Redirect(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState arclight$blockFade(Block block, BlockState stateIn, LevelReader worldIn, ScheduledTickAccess ticks, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        if (!(worldIn instanceof LevelAccessor accessor) || !CraftEventFactory.callBlockPhysicsEvent(accessor, currentPos).isCancelled()) {
            return block.defaultBlockState();
        }
        return super.updateShape(stateIn, worldIn, ticks, currentPos, facing, facingPos, facingState, random);
    }
}
