package io.izzel.arclight.neoforge.mixin.core.world.entity.animal;

import io.izzel.arclight.common.mixin.core.world.entity.animal.AnimalMixin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.animal.sheep.Sheep.class)
public abstract class SheepMixin_NeoForge extends AnimalMixin {
    @Inject(method = "lambda$shear$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/sheep/Sheep;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void arclight$forceDrop(ServerLevel level, ItemStack stack, CallbackInfo ci) { forceDrops = true; }

    @Inject(method = "lambda$shear$0", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/animal/sheep/Sheep;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void arclight$forceDropReset(ServerLevel level, ItemStack stack, CallbackInfo ci) { forceDrops = false; }
}
