package io.izzel.arclight.common.mixin.core.world.entity;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {

    // 26.1: value field removed; setValue exists but may be non-public on the compile classpath.
    @Invoker("setValue")
    void arclight$setValue(int value);
}
