package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.feline.Cat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.entity.animal.feline.Cat$CatRelaxOnOwnerGoal")
public class Cat_CatRelaxOnOwnerGoalMixin {

    @Shadow @Final private Cat cat;

    // 26.1: gift spawn uses ServerLevel.addFreshEntity inside a loot-table lambda.
    @Redirect(method = "lambda$giveMorningGift$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean arclight$dropItem(ServerLevel instance, Entity entity) {
        var event = new EntityDropItemEvent(((EntityBridge) this.cat).bridge$getBukkitEntity(), (Item) ((EntityBridge) entity).bridge$getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            return instance.addFreshEntity(entity);
        }
        return false;
    }
}
