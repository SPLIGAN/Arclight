package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dolphin.class)
public abstract class DolphinMixin extends PathfinderMobMixin {

    // 26.1: pickUpItem takes ServerLevel; Dolphin package under animal.dolphin.
    @Inject(method = "pickUpItem", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/dolphin/Dolphin;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void arclight$entityPick(ServerLevel level, ItemEntity itemEntity, CallbackInfo ci) {
        if (CraftEventFactory.callEntityPickupItemEvent((Dolphin) (Object) this, itemEntity, 0, false).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "pickUpItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"))
    private void arclight$pickCause(ServerLevel level, ItemEntity itemEntity, CallbackInfo ci) {
        ((EntityBridge) itemEntity).bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.PICKUP);
    }

    @Inject(method = "getMaxAirSupply", cancellable = true, at = @At("RETURN"))
    private void arclight$useBukkitMaxAir(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.maxAirTicks);
    }

    @Override
    public int getDefaultMaxAirSupply() {
        return Dolphin.TOTAL_AIR_SUPPLY;
    }
}
