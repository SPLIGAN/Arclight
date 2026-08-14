package io.izzel.arclight.common.mixin.core.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.Orientation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultRedstoneWireEvaluator.class)
public abstract class DefaultRedstoneWireEvaluatorMixin {

    // @formatter:off
    // 26.1: calculateTargetStrength moved from RedStoneWireBlock into the default evaluator.
    @Shadow private int calculateTargetStrength(Level world, BlockPos pos) { return 0; }
    // @formatter:on

    @Redirect(method = "updatePowerStrength", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/redstone/DefaultRedstoneWireEvaluator;calculateTargetStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I"))
    private int arclight$blockRedstone(DefaultRedstoneWireEvaluator self, Level world, BlockPos pos,
                                       Level world1, BlockPos pos1, BlockState state, Orientation orientation, boolean updateShape) {
        int i = this.calculateTargetStrength(world, pos);
        int oldPower = state.getValue(RedStoneWireBlock.POWER);
        if (oldPower != i) {
            BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(world, pos), oldPower, i);
            Bukkit.getPluginManager().callEvent(event);
            i = event.getNewCurrent();
        }
        return i;
    }
}
