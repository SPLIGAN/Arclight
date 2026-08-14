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

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 arclight$captureLavaContact(FluidState fluidState, BlockGetter level, BlockPos pos, Entity entity, boolean pushedByFluid) {
        if (fluidState.getType().is(FluidTags.LAVA)) {
            ((EntityBridge) entity).bridge$setLastLavaContact(pos.immutable());
        }
        return fluidState.getFlow(level, pos);
    }
}
