package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pig.class)
public abstract class PigMixin extends AnimalMixin {

    // 26.1: thunderHit uses convertTo; keep PigZapEvent cancel + LIGHTNING spawn/transform reasons.
    @Decorate(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/pig/Pig;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private Mob arclight$pigZap(Pig self, EntityType<? extends Mob> entityType, ConversionParams params, ConversionParams.AfterConversion<?> afterConversion, ServerLevel level, LightningBolt lightningBolt) throws Throwable {
        ((WorldBridge) level).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.LIGHTNING);
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.LIGHTNING);

        ConversionParams.AfterConversion<Mob> wrapped = mob -> {
            //noinspection unchecked,rawtypes
            ((ConversionParams.AfterConversion) afterConversion).finalizeConversion(mob);
            if (mob instanceof ZombifiedPiglin piglin
                    && CraftEventFactory.callPigZapEvent((Pig) (Object) this, lightningBolt, piglin).isCancelled()) {
                throw PigZapCancel.INSTANCE;
            }
        };

        try {
            return (Mob) DecorationOps.callsite().invoke(self, entityType, params, wrapped);
        } catch (Throwable t) {
            Throwable c = t;
            while (c != null) {
                if (c instanceof PigZapCancel) {
                    return null;
                }
                c = c.getCause();
            }
            throw t;
        }
    }

    private static final class PigZapCancel extends RuntimeException {
        private static final PigZapCancel INSTANCE = new PigZapCancel();

        private PigZapCancel() {
            super(null, null, false, false);
        }
    }
}
