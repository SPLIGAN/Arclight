package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(net.minecraft.world.entity.monster.zombie.ZombieVillager.class)
public abstract class ZombieVillagerMixin extends ZombieMixin {

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;removeEffect(Lnet/minecraft/core/Holder;)Z"))
    private void arclight$convert1(UUID conversionStarterIn, int conversionTimeIn, CallbackInfo ci) {
        this.persist = true;
        bridge$pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
    }

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void arclight$convert2(UUID conversionStarterIn, int conversionTimeIn, CallbackInfo ci) {
        bridge$pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
    }

    @Decorate(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private Mob arclight$cure(net.minecraft.world.entity.monster.zombie.ZombieVillager self, EntityType<? extends Mob> entityType, ConversionParams params, ConversionParams.AfterConversion<?> afterConversion) throws Throwable {
        ((WorldBridge) this.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.CURED);
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.CURED);
        Mob converted = (Mob) DecorationOps.callsite().invoke(self, entityType, params, afterConversion);
        if (converted == null) {
            ((ZombieVillager) ((EntityBridge) this).bridge$getBukkitEntity()).setConversionTime(-1);
        } else {
            ((LivingEntityBridge) converted).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
        }
        return converted;
    }

    @Inject(method = "lambda$finishConversion$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;dropPreservedEquipment(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Predicate;)Ljava/util/Set;"))
    private void arclight$dropPre(ServerLevel world, net.minecraft.world.entity.npc.villager.Villager villager, CallbackInfo ci) {
        this.forceDrops = true;
    }

    @Inject(method = "lambda$finishConversion$0", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;dropPreservedEquipment(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Predicate;)Ljava/util/Set;"))
    private void arclight$dropPost(ServerLevel world, net.minecraft.world.entity.npc.villager.Villager villager, CallbackInfo ci) {
        this.forceDrops = false;
    }
}
