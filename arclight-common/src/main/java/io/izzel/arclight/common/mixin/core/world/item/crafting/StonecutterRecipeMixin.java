package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import io.izzel.arclight.common.bridge.core.world.item.crafting.SingleItemRecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftStonecuttingRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StonecutterRecipe.class)
public abstract class StonecutterRecipeMixin implements RecipeBridge {

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        StonecutterRecipe recipe = (StonecutterRecipe) (Object) this;
        ItemStackTemplate result = ((SingleItemRecipeBridge) recipe).bridge$result();
        if (result.count() == 0) {
            return new ArclightSpecialRecipe(id, recipe);
        }
        CraftStonecuttingRecipe bukkit = new CraftStonecuttingRecipe(id, CraftItemStack.asCraftMirror(result.create()), CraftRecipe.toBukkit(recipe.input()));
        bukkit.setGroup(recipe.group());
        return bukkit;
    }
}
