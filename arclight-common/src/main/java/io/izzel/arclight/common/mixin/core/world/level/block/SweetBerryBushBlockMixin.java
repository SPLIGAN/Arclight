package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.world.damagesource.DamageSourceBridge;
import io.izzel.arclight.mixin.Eject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {

    @Unique
    private static final ThreadLocal<Player> arclight$harvestPlayer = new ThreadLocal<>();

    @Eject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean arclight$cropGrow(ServerLevel world, BlockPos pos, BlockState newState, int flags, CallbackInfo ci) {
        if (!CraftEventFactory.handleBlockGrowEvent(world, pos, newState, flags)) {
            ci.cancel();
        }
        return true;
    }

    @Redirect(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;sweetBerryBush()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource arclight$blockDamage(DamageSources instance, BlockState blockState, Level level, BlockPos blockPos) {
        return ((DamageSourceBridge) instance.sweetBerryBush()).bridge$directBlock(CraftBlock.at(level, blockPos));
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void arclight$captureHarvester(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        arclight$harvestPlayer.set(player);
    }

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void arclight$clearHarvester(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        arclight$harvestPlayer.remove();
    }

    // 26.1: harvest drops via loot table callback (lambda$useWithoutItem$0 -> Block.popResource).
    @Redirect(method = "lambda$useWithoutItem$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private static void arclight$playerHarvest(Level worldIn, BlockPos pos, ItemStack stack) {
        Player player = arclight$harvestPlayer.get();
        if (player == null) {
            Block.popResource(worldIn, pos, stack);
            return;
        }
        PlayerHarvestBlockEvent event = CraftEventFactory.callPlayerHarvestBlockEvent(worldIn, pos, player, InteractionHand.MAIN_HAND, Collections.singletonList(stack));
        if (!event.isCancelled()) {
            for (org.bukkit.inventory.ItemStack itemStack : event.getItemsHarvested()) {
                Block.popResource(worldIn, pos, CraftItemStack.asNMSCopy(itemStack));
            }
        }
    }
}
