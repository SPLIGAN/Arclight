package io.izzel.arclight.common.mixin.core.world.item.consume_effects;

import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyStatusEffectsConsumeEffect.class)
public class ApplyStatusEffectsConsumeEffectMixin {

    // 26.1: LivingEntity.addEatEffect removed; food status effects apply via ConsumeEffect
    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void arclight$foodEffectCause(Level level, ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        ((LivingEntityBridge) entity).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.FOOD);
    }
}
