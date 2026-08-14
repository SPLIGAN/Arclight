package io.izzel.arclight.common.mixin.core.world.item;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    // 26.1: projectiles spawn via Projectile.spawnProjectile instead of ServerLevel.addFreshEntity.
    @Decorate(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private <T extends Projectile> T arclight$shootBow(T projectile, ServerLevel level, ItemStack ammo, Consumer<T> configure,
                                                       ServerLevel serverLevel, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack weapon,
                                                       List<ItemStack> projectiles, float power, float uncertainty, boolean crit, LivingEntity target) throws Throwable {
        var event = CraftEventFactory.callEntityShootBowEvent(livingEntity, weapon, ammo, projectile, interactionHand, power, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return (T) DecorationOps.cancel().invoke();
        }

        if (event.getProjectile() == ((EntityBridge) projectile).bridge$getBukkitEntity()) {
            T spawned = (T) DecorationOps.callsite().invoke(projectile, level, ammo, configure);
            if (spawned == null && livingEntity instanceof net.minecraft.server.level.ServerPlayer) {
                ((ServerPlayerBridge) livingEntity).bridge$getBukkitEntity().updateInventory();
            }
            return spawned;
        }
        return projectile;
    }
}
