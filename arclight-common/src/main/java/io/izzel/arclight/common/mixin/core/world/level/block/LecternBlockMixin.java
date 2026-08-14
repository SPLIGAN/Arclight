package io.izzel.arclight.common.mixin.core.world.level.block;

import net.minecraft.world.level.block.LecternBlock;
import org.spongepowered.asm.mixin.Mixin;

// 26.1: popBook moved to LecternBlockEntity.preRemoveSideEffects (see LecternBlockEntityMixin).
@Mixin(LecternBlock.class)
public class LecternBlockMixin {
}
