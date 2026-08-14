package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.MobBridge;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PigZombieAngerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin extends ZombieMixin {

    // @formatter:off
    // 26.1: anger target is EntityReference; duration is set via setTimeToRemainAngry(J).
    @Shadow public abstract EntityReference<LivingEntity> getPersistentAngerTarget();
    @Shadow public abstract long getPersistentAngerEndTime();
    // @formatter:on

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    private void alertOthers() {
        double d0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB axisalignedbb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        for (ZombifiedPiglin piglinEntity : this.level().getEntitiesOfClass(ZombifiedPiglin.class, axisalignedbb)) {
            if (piglinEntity != (Object) this) {
                if (piglinEntity.getTarget() == null) {
                    if (!piglinEntity.isAlliedTo(this.getTarget())) {
                        ((MobBridge) piglinEntity).bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
                        piglinEntity.setTarget(this.getTarget());
                    }
                }
            }
        }
    }

    @ModifyArg(method = "startPersistentAngerTimer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombifiedPiglin;setTimeToRemainAngry(J)V"))
    private long arclight$pigAngry(long time) {
        LivingEntity living = EntityReference.getLivingEntity(this.getPersistentAngerTarget(), this.level());
        PigZombieAngerEvent event = new PigZombieAngerEvent((PigZombie) this.getBukkitEntity(), living == null ? null : ((EntityBridge) living).bridge$getBukkitEntity(), (int) Math.min(Integer.MAX_VALUE, time));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            long end = this.getPersistentAngerEndTime();
            if (end < 0L) {
                return 0L;
            }
            return Math.max(0L, end - this.level().getGameTime());
        }
        return event.getNewAnger();
    }
}
