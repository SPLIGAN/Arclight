package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.IngredientBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IngredientBridge {

    public boolean exact;

    @Decorate(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/core/HolderSet;)Z"))
    private boolean arclight$exactMatch(ItemStack instance, HolderSet<Item> holders, ItemStack itemstack) throws Throwable {
        if (exact) {
            for (Holder<Item> holder : ((Ingredient) (Object) this).items().toList()) {
                if (ItemStack.isSameItemSameComponents(itemstack, holder.value().getDefaultInstance())) {
                    return (boolean) DecorationOps.cancel().invoke(true);
                }
            }
            return false;
        }
        return (boolean) DecorationOps.callsite().invoke(instance, holders);
    }

    @Override
    public void bridge$setExact(boolean exact) {
        this.exact = exact;
    }

    @Override
    public boolean bridge$isExact() {
        return this.exact;
    }
}
