package io.izzel.arclight.common.mixin.core.world.item;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.world.entity.projectile.ThrownTridentBridge;
import io.izzel.arclight.common.mod.util.DistValidate;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public class TridentItemMixin {

    // 26.1: throw path uses Projectile.spawnProjectileFromRotation (no Level.addFreshEntity / hurtAndBreak).
    @Decorate(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile arclight$addEntity(Projectile.ProjectileFactory<?> factory, ServerLevel level, ItemStack thrownStack, LivingEntity shooter, float roll, float power, float uncertainty,
                                          ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) throws Throwable {
        Projectile spawned = (Projectile) DecorationOps.callsite().invoke(factory, level, thrownStack, shooter, roll, power, uncertainty);
        if (spawned == null || !((EntityBridge) spawned).bridge$isInWorld()) {
            if (entityLiving instanceof ServerPlayer) {
                ((ServerPlayerBridge) entityLiving).bridge$getBukkitEntity().updateInventory();
            }
            return (Projectile) DecorationOps.cancel().invoke(false);
        }
        ((ThrownTridentBridge) spawned).bridge$setThrownStack(thrownStack.copy());
        return spawned;
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;push(DDD)V"))
    private void arclight$riptide(Player instance, double x, double y, double z, ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!DistValidate.isValid(worldIn)) return;
        CraftEventFactory.callPlayerRiptideEvent(instance, stack, (float) x, (float) y, (float) z);
        instance.push(x, y, z);
    }
}
