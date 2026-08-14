package io.izzel.arclight.neoforge.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mod.util.ArclightBridges;
import io.izzel.arclight.neoforge.mixin.core.world.entity.MobMixin_NeoForge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MushroomCow.class)
public abstract class MushroomCowMixin_NeoForge extends MobMixin_NeoForge {

    @Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/cow/MushroomCow;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private void arclight$pushShearTransform(ServerLevel level, SoundSource soundSource, ItemStack tool, CallbackInfo ci) {
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.SHEARED);
        ((WorldBridge) this.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SHEARED);
    }

    @Redirect(method = "lambda$shear$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/cow/MushroomCow;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity arclight$onShearDrop(MushroomCow cow, ServerLevel level, ItemStack stack, float yOffset) {
        var itemEntity = new ItemEntity(level, cow.getX(), cow.getY(yOffset), cow.getZ(), stack);
        EntityDropItemEvent event = new EntityDropItemEvent(this.bridge$getBukkitEntity(), (org.bukkit.entity.Item) ArclightBridges.toBukkit(itemEntity));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        level.addFreshEntity(itemEntity);
        return itemEntity;
    }
}
