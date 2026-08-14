package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import io.izzel.arclight.common.mixin.optimization.general.activationrange.EntityMixin_ActivationRange;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin_ActivationRange extends EntityMixin_ActivationRange {

    // @formatter:off
    // 26.1: inGround moved to synched data accessors.
    @Shadow protected abstract boolean isInGround();
    @Shadow protected int inGroundTime;
    // @formatter:on

    @Override
    public void inactiveTick() {
        super.inactiveTick();
        if (this.isInGround()) {
            this.inGroundTime++;
        }
    }
}
