package io.izzel.arclight.neoforge.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionMixin_NeoForge {

    /**
     * NeoForge 26.1.2.103+ delegates {@code update(Entity, boolean)} to
     * {@code update(Entity, Predicate)} where {@link FluidState#getFlow} lives.
     */
    @Redirect(
        method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)V",
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 arclight$captureLavaContact(FluidState fluidState, BlockGetter level, BlockPos pos, Entity entity, Predicate<FluidType> typePushPredicate) {
        if (fluidState.getType().is(FluidTags.LAVA)) {
            ((EntityBridge) entity).bridge$setLastLavaContact(pos.immutable());
        }
        return fluidState.getFlow(level, pos);
    }
}
