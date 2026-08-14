package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public interface LeashableMixin {

    // 26.1: writeLeashData uses ValueOutput instead of CompoundTag.
    @Decorate(method = "writeLeashData", inject = true, at = @At("HEAD"))
    private void arclight$skipRemoved(ValueOutput output, Leashable.LeashData leashData) throws Throwable {
        if (leashData != null && leashData.leashHolder != null && ((EntityBridge) leashData.leashHolder).bridge$pluginRemoved()) {
            DecorationOps.cancel().invoke();
            return;
        }
        DecorationOps.blackhole().invoke();
    }

    @Decorate(method = "restoreLeashFromSave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static ItemEntity arclight$forceDrop(Entity instance, ServerLevel level, ItemLike itemLike) throws Throwable {
        ((EntityBridge) instance).bridge$setForceDrops(true);
        var itemEntity = (ItemEntity) DecorationOps.callsite().invoke(instance, level, itemLike);
        ((EntityBridge) instance).bridge$setForceDrops(false);
        return itemEntity;
    }

    @Decorate(method = "tickLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;dropLeash()V"))
    private static void arclight$unleashDrop(Leashable self) throws Throwable {
        if (self instanceof Entity entity) {
            Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(((EntityBridge) entity).bridge$getBukkitEntity(),
                entity.isAlive() ? EntityUnleashEvent.UnleashReason.HOLDER_GONE : EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH));
        }
        DecorationOps.callsite().invoke(self);
    }

    @Decorate(method = "tickLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;removeLeash()V"))
    private static void arclight$unleashRemove(Leashable self) throws Throwable {
        if (self instanceof Entity entity) {
            Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(((EntityBridge) entity).bridge$getBukkitEntity(),
                entity.isAlive() ? EntityUnleashEvent.UnleashReason.HOLDER_GONE : EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH));
        }
        DecorationOps.callsite().invoke(self);
    }

    // Keep drop item force-drops on the shared static dropLeash(E,ZZ) implementation.
    @Decorate(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static ItemEntity arclight$forceDrop2(Entity instance, ServerLevel level, ItemLike itemLike) throws Throwable {
        ((EntityBridge) instance).bridge$setForceDrops(true);
        var itemEntity = (ItemEntity) DecorationOps.callsite().invoke(instance, level, itemLike);
        ((EntityBridge) instance).bridge$setForceDrops(false);
        return itemEntity;
    }

    @Decorate(method = "leashTooFarBehaviour", inject = true, at = @At("HEAD"))
    private void arclight$distanceLeash() {
        if (this instanceof Entity entity) {
            Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(((EntityBridge) entity).bridge$getBukkitEntity(), EntityUnleashEvent.UnleashReason.DISTANCE));
        }
    }
}
