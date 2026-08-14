package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmallFireball.class)
public abstract class SmallFireballMixin extends AbstractHurtingProjectileMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;)V", at = @At("RETURN"))
    private void arclight$init(Level level, LivingEntity livingEntity, Vec3 vec3, CallbackInfo ci) {
        if (this.getOwner() != null && this.getOwner() instanceof Mob) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                this.isIncendiary = serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);
            }
        }
    }

    @Decorate(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void arclight$entityCombust(Entity entity, float seconds) throws Throwable {
        EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(), ((EntityBridge) entity).bridge$getBukkitEntity(), seconds);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            DecorationOps.callsite().invoke(entity, event.getDuration());
        }
    }

    @Decorate(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean arclight$burnBlock(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) throws Throwable {
        if (!this.isIncendiary || CraftEventFactory.callBlockIgniteEvent(this.level(), pos, (SmallFireball) (Object) this).isCancelled()) {
            return false;
        }
        return (boolean) DecorationOps.callsite().invoke(level, pos, state);
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/hurtingprojectile/SmallFireball;discard()V"))
    private void arclight$hitCause(HitResult hitResult, CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.HIT);
    }
}
