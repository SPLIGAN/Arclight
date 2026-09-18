package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionMixin {

    // NeoForge 26.1.2.103+ moved getFlow into update(Entity, Predicate); see EntityFluidInteractionMixin_NeoForge.
    @Redirect(method = "update(Lnet/minecraft/world/entity/Entity;Z)V", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 arclight$captureLavaContact(FluidState fluidState, BlockGetter level, BlockPos pos, Entity entity, boolean ignoreCurrent) {
        if (fluidState.getType().is(FluidTags.LAVA)) {
            ((EntityBridge) entity).bridge$setLastLavaContact(pos.immutable());
        }
        return fluidState.getFlow(level, pos);
    }
}
