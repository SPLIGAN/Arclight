package io.izzel.arclight.common.mixin.core.world.entity.vehicle;

import io.izzel.arclight.common.bridge.core.world.entity.vehicle.AbstractMinecartBridge;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin {

    @Redirect(method = "getSlowdownFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;isVehicle()Z"))
    private boolean arclight$slowWhenEmpty(AbstractMinecart cart) {
        return cart.isVehicle() || !((AbstractMinecartBridge) cart).bridge$slowWhenEmpty();
    }
}
