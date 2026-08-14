package io.izzel.arclight.common.mixin.core.world.effect;

import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.1.2: MobEffect tick/instant APIs take ServerLevel as first argument.
@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public class HealOrHarmMobEffectMixin {

    @Inject(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void arclight$healReason1(ServerLevel level, LivingEntity livingEntity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        ((LivingEntityBridge) livingEntity).bridge$pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC);
    }

    @Inject(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void arclight$healReason2(ServerLevel level, Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo ci) {
        ((LivingEntityBridge) livingEntity).bridge$pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC);
    }
}
