package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {

    @Shadow @Final public static IntegerProperty HATCH;

    // 26.1: playSound listener is Entity (not Player). Cancel before crack sound if grow is cancelled.
    @Inject(method = "randomTick", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void arclight$hatch(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!CraftEventFactory.handleBlockGrowEvent(worldIn, pos, state.setValue(HATCH, state.getValue(HATCH) + 1), 2)) {
            ci.cancel();
        }
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean arclight$hatchGrow(ServerLevel world, BlockPos pos, BlockState newState, int flags) {
        // Already applied by handleBlockGrowEvent in arclight$hatch.
        return true;
    }

    @Inject(method = "randomTick", cancellable = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void arclight$born(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (CraftEventFactory.callBlockFadeEvent(worldIn, pos, Blocks.AIR.defaultBlockState()).isCancelled()) {
            ci.cancel();
        } else {
            ((WorldBridge) worldIn).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.EGG);
        }
    }

    @Inject(method = "destroyEgg", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TurtleEggBlock;decreaseEggs(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    public void arclight$stepOn(Level world, BlockState state, BlockPos pos, Entity entity, int i, CallbackInfo ci) {
        Cancellable cancellable;
        if (entity instanceof Player) {
            cancellable = CraftEventFactory.callPlayerInteractEvent((Player) entity, Action.PHYSICAL, pos, null, null, null);
        } else {
            cancellable = new EntityInteractEvent(((EntityBridge) entity).bridge$getBukkitEntity(), CraftBlock.at(world, pos));
            Bukkit.getPluginManager().callEvent((EntityInteractEvent) cancellable);
        }

        if (cancellable.isCancelled()) {
            ci.cancel();
        }
    }
}
