package io.izzel.arclight.common.mixin.core.world.entity;

import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Minecraft 26.1 inserted {@link Avatar} between {@link net.minecraft.world.entity.player.Player}
 * and {@link net.minecraft.world.entity.LivingEntity}. Mixin hierarchy must mirror that or
 * Player/ServerPlayer transform fails at first login ({@code MixinTransformerError}).
 */
@Mixin(Avatar.class)
public abstract class AvatarMixin extends LivingEntityMixin {
}
