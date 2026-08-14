package io.izzel.arclight.common.mixin.vanilla.world.entity.animal;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.MobBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mixin.vanilla.world.entity.EntityMixin_Vanilla;
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
public abstract class MushroomCowMixin_Vanilla extends EntityMixin_Vanilla {

    @Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/cow/MushroomCow;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
    private void arclight$pushShearTransform(ServerLevel level, SoundSource source, ItemStack tool, CallbackInfo ci) {
        ((MobBridge) this).bridge$pushTransformReason(EntityTransformEvent.TransformReason.SHEARED);
        ((WorldBridge) this.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SHEARED);
    }

    @Redirect(method = "lambda$shear$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/cow/MushroomCow;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity arclight$shearDrop(MushroomCow cow, ServerLevel level, ItemStack stack, float yOffset) {
        var itemEntity = new ItemEntity(level, cow.getX(), cow.getY(yOffset), cow.getZ(), stack);
        EntityDropItemEvent event = new EntityDropItemEvent(((EntityBridge) this).bridge$getBukkitEntity(), (org.bukkit.entity.Item) ((EntityBridge) itemEntity).bridge$getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        level.addFreshEntity(itemEntity);
        return itemEntity;
    }
}
