package io.izzel.arclight.common.mixin.core.world.entity.animal.frog;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tadpole.class)
public abstract class TadpoleMixin extends PathfinderMobMixin {

    // @formatter:off
    @Shadow protected abstract void setAge(int i);
    // @formatter:on

    // 26.1: metamorphosis uses convertTo (MobMixin fires transform/remove events).
    @Inject(method = "ageUp()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Tadpole;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private void arclight$transform(CallbackInfo ci) {
        ((WorldBridge) this.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.METAMORPHOSIS);
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.METAMORPHOSIS);
    }

    @Inject(method = "ageUp()V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/animal/frog/Tadpole;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private void arclight$transformCancelled(CallbackInfo ci) {
        // Avoid ageUp loop when EntityTransformEvent cancels convertTo (returns null / tadpole kept).
        if (!this.isRemoved()) {
            this.setAge(0);
        }
    }
}
