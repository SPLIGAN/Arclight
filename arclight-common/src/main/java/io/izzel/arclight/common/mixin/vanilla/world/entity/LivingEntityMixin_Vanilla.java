package io.izzel.arclight.common.mixin.vanilla.world.entity;

import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.vanilla.world.entity.LivingEntityBridge_Vanilla;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.ArclightDamageContainer;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric / vanilla-platform damage pipeline for MC 26.1+.
 * Uses {@code hurtServer} / {@code actuallyHurt(ServerLevel, ...)} — the pre-26.1
 * {@code hurt(DamageSource, float)} entry point no longer exists.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Vanilla extends EntityMixin_Vanilla implements LivingEntityBridge, LivingEntityBridge_Vanilla {

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void arclight$startCapture(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
        this.arclight$startCaptureDrops();
    }

    @Inject(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V"))
    private void arclight$stopCapture(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
        final var list = this.arclight$finishCaptureDrops();
        this.arclight$vanilla$callLivingDropsEvent(damageSource, list);
        list.forEach(serverLevel::addFreshEntity);
    }

    @Decorate(method = "hurtServer", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"))
    private void arclight$entityDamageEvent(ServerLevel level, DamageSource damagesource, float originalDamage, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        EntityDamageEvent event = arclight$fireEntityDamageEvent(damagesource, originalDamage);

        if (event == null || event.isCancelled()) {
            DecorationOps.cancel().invoke(false);
            return;
        }

        container = new ArclightDamageContainer(event);
        originalDamage = (float) event.getDamage();
        DecorationOps.blackhole().invoke(container, originalDamage);

        if (damagesource.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            player.resetAttackStrengthTicker();
        }
    }

    // 26.1: shield amount comes from applyItemBlocking (isDamageSourceBlocked removed).
    @Decorate(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float arclight$vanilla$postApplyShield(LivingEntity blocker, ServerLevel level, DamageSource source, float damage, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer arclight) throws Throwable {
        float vanillaBlocked = (float) DecorationOps.callsite().invoke(blocker, level, source, damage);
        float bukkit = -(float) arclight.getBukkit().getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
        if (bukkit > 0.0F) {
            arclight.applyOffset(-bukkit);
            return bukkit;
        }
        return vanillaBlocked > 0.0F ? 0.0F : vanillaBlocked;
    }

    // ordinal 1 = DamageTypeTags.IS_FREEZING inside hurtServer
    @Decorate(method = "hurtServer", inject = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    private void arclight$vanilla$postApplyFreezing(ServerLevel level, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        original = container.calculateStage(EntityDamageEvent.DamageModifier.FREEZING, original);
        DecorationOps.blackhole().invoke(original);
    }

    // ordinal 2 = DamageTypeTags.DAMAGES_HELMET inside hurtServer
    @Decorate(method = "hurtServer", inject = true, at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    private void arclight$vanilla$postApplyHardHat(ServerLevel level, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        original = container.calculateStage(EntityDamageEvent.DamageModifier.HARD_HAT, original);
        DecorationOps.blackhole().invoke(original);
    }

    @Decorate(method = "hurtServer", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void arclight$vanilla$captureEntityDamageEvent(ServerLevel level, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        ArclightCaptures.captureDamageContainer(container);
    }

    @Decorate(method = "actuallyHurt", inject = true, at = @At("HEAD"))
    private void arclight$vanilla$getEntityDamageEvent(ServerLevel level, DamageSource damageSource, float f, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        container = ArclightCaptures.getDamageContainer();
        DecorationOps.blackhole().invoke(container);
    }

    @Decorate(method = "actuallyHurt", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private void arclight$vanilla$postApplyArmor(ServerLevel level, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        original = container.calculateStage(EntityDamageEvent.DamageModifier.ARMOR, original);
        DecorationOps.blackhole().invoke(original);
    }

    @Decorate(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float arclight$vanilla$postApplyResistance(float first, float second) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(first, second);
        result = ArclightCaptures.getDamageContainer().calculateStage(EntityDamageEvent.DamageModifier.RESISTANCE, result);
        return result;
    }

    @Decorate(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float arclight$vanilla$postApplyMagic(LivingEntity entity, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(entity, source, original);
        return container.calculateStage(EntityDamageEvent.DamageModifier.MAGIC, result);
    }

    @Decorate(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float arclight$vanilla$postApplyAbsorption(float first, float second, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(first, second);
        result = container.calculateStage(EntityDamageEvent.DamageModifier.ABSORPTION, result);
        return Math.max(result, 0.0F);
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void arclight$vanilla$popEntityDamageEvent(ServerLevel level, DamageSource arg, float g, CallbackInfo ci) {
        ArclightCaptures.popDamageContainer();
    }
}
