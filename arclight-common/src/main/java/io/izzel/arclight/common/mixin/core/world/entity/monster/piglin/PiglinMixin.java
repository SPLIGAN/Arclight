package io.izzel.arclight.common.mixin.core.world.entity.monster.piglin;

import com.mojang.serialization.Codec;
import io.izzel.arclight.common.bridge.core.world.entity.monster.piglin.PiglinBridge;
import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mixin(Piglin.class)
public abstract class PiglinMixin extends PathfinderMobMixin implements PiglinBridge {

    public Set<Item> allowedBarterItems = new HashSet<>();
    public Set<Item> interestItems = new HashSet<>();

    @Override
    public Set<Item> bridge$getAllowedBarterItems() {
        return allowedBarterItems;
    }

    @Override
    public Set<Item> bridge$getInterestItems() {
        return interestItems;
    }

    // 26.1: persist custom item sets via ValueOutput string lists.
    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void arclight$writeAdditional(ValueOutput output, CallbackInfo ci) {
        var barterList = output.list("Bukkit.BarterList", Codec.STRING);
        for (Item item : allowedBarterItems) {
            var key = BuiltInRegistries.ITEM.getKey(item);
            if (key != null) {
                barterList.add(key.toString());
            }
        }
        var interestList = output.list("Bukkit.InterestList", Codec.STRING);
        for (Item item : interestItems) {
            var key = BuiltInRegistries.ITEM.getKey(item);
            if (key != null) {
                interestList.add(key.toString());
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void arclight$readAdditional(ValueInput input, CallbackInfo ci) {
        this.allowedBarterItems = StreamSupport.stream(input.listOrEmpty("Bukkit.BarterList", Codec.STRING).spliterator(), false)
            .map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .map(BuiltInRegistries.ITEM::getValue)
            .collect(Collectors.toCollection(HashSet::new));
        this.interestItems = StreamSupport.stream(input.listOrEmpty("Bukkit.InterestList", Codec.STRING).spliterator(), false)
            .map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .map(BuiltInRegistries.ITEM::getValue)
            .collect(Collectors.toCollection(HashSet::new));
    }

    // 26.1: offhand currency check is ItemStack.isPiglinCurrency() (NeoForge extension).
    @Decorate(method = "holdInOffHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isPiglinCurrency()Z"))
    private boolean arclight$customBarter(ItemStack itemStack) throws Throwable {
        return (boolean) DecorationOps.callsite().invoke(itemStack) || allowedBarterItems.contains(itemStack.getItem());
    }

    // 26.1: canReplaceCurrentItem gained EquipmentSlot.
    @Redirect(method = "canReplaceCurrentItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isLovedItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean arclight$customLoved(ItemStack stack) {
        return PiglinAi.isLovedItem(stack) || interestItems.contains(stack.getItem()) || allowedBarterItems.contains(stack.getItem());
    }
}
