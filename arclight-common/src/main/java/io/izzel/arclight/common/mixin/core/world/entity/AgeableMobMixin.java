package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.world.entity.AgeableMobBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin extends PathfinderMobMixin implements AgeableMobBridge {

    // @formatter:off
    @Shadow public abstract boolean isBaby();
    @Shadow @Nullable public abstract AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate);
    @Shadow public abstract void setAge(int age);
    @Shadow public abstract boolean isAgeLocked();
    // @formatter:on

    // 26.1: AgeLocked is first-class (synched + NBT + canAgeUp). Bridge to vanilla; no isClientSide field redirect.
    @Override
    public boolean bridge$isAgeLocked() {
        return this.isAgeLocked();
    }
}
