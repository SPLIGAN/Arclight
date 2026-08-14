package io.izzel.arclight.common.mixin.core.world.item;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EggItem.class)
public abstract class EggItemMixin extends Item {

    public EggItemMixin(Properties properties) {
        super(properties);
    }

    // 26.1: playSound first arg is Entity.
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void arclight$muteSound(Level world, Entity player, double x, double y, double z, SoundEvent soundIn, SoundSource category, float volume, float pitch) {
    }

    // 26.1: eggs spawn via Projectile.spawnProjectileFromRotation.
    @Decorate(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile arclight$updateIfFail(Projectile.ProjectileFactory<?> factory, ServerLevel level, ItemStack stack, LivingEntity shooter, float roll, float power, float uncertainty,
                                             Level worldIn, Player playerIn, @NotNull InteractionHand handIn) throws Throwable {
        Projectile spawned = (Projectile) DecorationOps.callsite().invoke(factory, level, stack, shooter, roll, power, uncertainty);
        if (spawned == null || !((EntityBridge) spawned).bridge$isInWorld()) {
            if (playerIn instanceof ServerPlayerBridge) {
                ((ServerPlayerBridge) playerIn).bridge$getBukkitEntity().updateInventory();
            }
            return (Projectile) DecorationOps.cancel().invoke((InteractionResult) InteractionResult.FAIL);
        }
        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (worldIn.getRandom().nextFloat() * 0.4F + 0.8F));
        return spawned;
    }
}
