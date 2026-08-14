package io.izzel.arclight.neoforge.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.ArclightDamageContainer;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.EventHooks;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Stack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_NeoForge extends EntityMixin_NeoForge implements LivingEntityBridge {

    // @formatter:off
    @Shadow protected abstract void dropExperience(ServerLevel level, @Nullable Entity entity);
    @Shadow protected Stack<DamageContainer> damageContainers;
    // @formatter:on

    @Redirect(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V"))
    private void arclight$dropLater(LivingEntity instance, ServerLevel level, Entity entity) {
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void arclight$dropLast(ServerLevel arg, DamageSource damageSource, CallbackInfo ci) {
        this.dropExperience(arg, damageSource.getEntity());
    }

    @Decorate(method = "hurtServer", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/neoforged/neoforge/common/damagesource/DamageContainer;getNewDamage()F"))
    private float arclight$neoforge$entityDamageEvent(DamageContainer instance, ServerLevel level, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(instance);
        final EntityDamageEvent event = arclight$fireEntityDamageEvent(source, result);

        if (event == null || event.isCancelled()) {
            this.damageContainers.pop();
            return (float) DecorationOps.cancel().invoke(false);
        }

        container = new ArclightDamageContainer(event);
        DecorationOps.blackhole().invoke(container);
        damageContainers.peek().setNewDamage((float) event.getDamage());

        if (source.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            player.resetAttackStrengthTicker();
        }
        return result;
    }

    // 26.1: CommonHooks.onDamageBlock removed; blocking amount comes from applyItemBlocking.
    @Decorate(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float arclight$neoforge$postApplyShield(LivingEntity blocker, ServerLevel level, DamageSource source, float damage, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer arclight) throws Throwable {
        float vanillaBlocked = (float) DecorationOps.callsite().invoke(blocker, level, source, damage);
        float bukkit = -(float) arclight.getBukkit().getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
        if (bukkit > 0.0F) {
            arclight.applyOffset(-bukkit);
            return bukkit;
        }
        // Bukkit says not blocking — suppress vanilla blocked amount (old setBlocked(false)).
        return vanillaBlocked > 0.0F ? 0.0F : vanillaBlocked;
    }

    @Decorate(method = "hurtServer", inject = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    private void arclight$neoforge$postApplyFreezing(ServerLevel level, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        original = container.calculateStage(EntityDamageEvent.DamageModifier.FREEZING, original);
        DecorationOps.blackhole().invoke(original);
    }

    @Decorate(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/DamageContainer;setNewDamage(F)V"))
    private void arclight$neoforge$postApplyHardHat(DamageContainer container, float arg, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer arclight) throws Throwable {
        arg = arclight.calculateStage(EntityDamageEvent.DamageModifier.HARD_HAT, arg);
        DecorationOps.callsite().invoke(container, arg);
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

    @Decorate(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float arclight$neoforge$postApplyArmor(LivingEntity entity, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer container) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(entity, source, original);
        result = container.calculateStage(EntityDamageEvent.DamageModifier.ARMOR, result);
        return result;
    }

    @Decorate(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float arclight$neoforge$postApplyResistance(float first, float second) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(first, second);
        result = ArclightCaptures.getDamageContainer().calculateStage(EntityDamageEvent.DamageModifier.RESISTANCE, result);
        return result;
    }

    @Decorate(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float arclight$neoforge$postApplyMagic(LivingEntity entity, DamageSource source, float original, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer arclight) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(entity, source, original);
        float newResult = arclight.calculateStage(EntityDamageEvent.DamageModifier.MAGIC, result);
        if (Math.abs(result - newResult) > Mth.EPSILON) {
            DamageContainer container = damageContainers.peek();
            container.setNewDamage(original);
            container.setReduction(DamageContainer.Reduction.ENCHANTMENTS, original - newResult);
        }
        return newResult;
    }

    @Decorate(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;onLivingDamagePre(Lnet/minecraft/world/entity/LivingEntity;Lnet/neoforged/neoforge/common/damagesource/DamageContainer;)F"))
    private float arclight$neoforge$applyFromLivingDamagePre(LivingEntity entity, DamageContainer container, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer arclight) throws Throwable {
        float result = (float) DecorationOps.callsite().invoke(entity, container);
        arclight.setCurrentDamage(result);
        return result;
    }

    @Decorate(method = "actuallyHurt", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/neoforged/neoforge/common/damagesource/DamageContainer;setReduction(Lnet/neoforged/neoforge/common/damagesource/DamageContainer$Reduction;F)V"))
    private void arclight$vanilla$postApplyAbsorption(DamageContainer container, DamageContainer.Reduction reduction, float amount, @Local(allocate = "arclightDamageContainer") ArclightDamageContainer arclight) throws Throwable {
        float currentDamage = damageContainers.peek().getNewDamage();
        float exactDamage = currentDamage - amount;
        float afterDamage = arclight.calculateStage(EntityDamageEvent.DamageModifier.ABSORPTION, exactDamage);
        if (Math.abs(afterDamage - exactDamage) > Mth.EPSILON) {
            amount = currentDamage - afterDamage;
        }
        DecorationOps.callsite().invoke(container, reduction, amount);
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void arclight$vanilla$popEntityDamageEvent(ServerLevel level, DamageSource arg, float g, CallbackInfo ci) {
        ArclightCaptures.popDamageContainer();
    }

    @Override
    public boolean bridge$forge$onLivingUseTotem(LivingEntity entity, DamageSource damageSource, ItemStack totem, InteractionHand hand) {
        return CommonHooks.onLivingUseTotem(entity, damageSource, totem, hand);
    }

    @Override
    public void bridge$forge$onLivingConvert(LivingEntity entity, LivingEntity outcome) {
        EventHooks.onLivingConvert(entity, outcome);
    }

    @Override
    public boolean bridge$forge$canEntityDestroy(Level level, BlockPos pos, LivingEntity entity) {
        return level instanceof net.minecraft.server.level.ServerLevel serverLevel && CommonHooks.canEntityDestroy(serverLevel, pos, entity);
    }
}
