package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BannerBlockEntity.class)
public abstract class BannerBlockEntityMixin extends BlockEntity {

    @Shadow private BannerPatternLayers patterns;

    public BannerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    // 26.1: patterns assigned inline in loadAdditional; cap layer count like CraftBukkit.
    @Decorate(method = "loadAdditional", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BannerBlockEntity;patterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;", opcode = Opcodes.PUTFIELD))
    private void arclight$setPatterns(BannerBlockEntity self, BannerPatternLayers layers) throws Throwable {
        DecorationOps.callsite().invoke(self, this.arclight$limitPatterns(layers));
    }

    @Decorate(method = "applyImplicitComponents", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BannerBlockEntity;patterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;", opcode = Opcodes.PUTFIELD))
    private void arclight$applyLimits(BannerBlockEntity self, BannerPatternLayers layers, DataComponentGetter dataComponentGetter) throws Throwable {
        DecorationOps.callsite().invoke(self, this.arclight$limitPatterns(layers));
    }

    // CraftBukkit start
    public void setPatterns(BannerPatternLayers bannerpatternlayers) {
        this.patterns = this.arclight$limitPatterns(bannerpatternlayers);
    }

    private BannerPatternLayers arclight$limitPatterns(BannerPatternLayers bannerpatternlayers) {
        if (bannerpatternlayers.layers().size() > 20) {
            return new BannerPatternLayers(List.copyOf(bannerpatternlayers.layers().subList(0, 20)));
        }
        return bannerpatternlayers;
    }
    // CraftBukkit end
}
