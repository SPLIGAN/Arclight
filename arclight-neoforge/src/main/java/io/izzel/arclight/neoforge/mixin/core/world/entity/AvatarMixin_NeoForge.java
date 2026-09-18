package io.izzel.arclight.neoforge.mixin.core.world.entity;

import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;

/**
 * NeoForge-side bridge so {@code PlayerMixin_NeoForge} mirrors {@code Player → Avatar → LivingEntity}.
 */
@Mixin(Avatar.class)
public abstract class AvatarMixin_NeoForge extends LivingEntityMixin_NeoForge {
}
