package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import io.izzel.arclight.common.bridge.core.world.item.crafting.SingleItemRecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftCampfireRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CampfireCookingRecipe.class)
public abstract class CampfireCookingRecipeMixin implements RecipeBridge {

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        CampfireCookingRecipe recipe = (CampfireCookingRecipe) (Object) this;
        ItemStackTemplate result = ((SingleItemRecipeBridge) recipe).bridge$result();
        if (result.count() == 0) {
            return new ArclightSpecialRecipe(id, recipe);
        }
        CraftCampfireRecipe bukkit = new CraftCampfireRecipe(id, CraftItemStack.asCraftMirror(result.create()), CraftRecipe.toBukkit(recipe.input()), recipe.experience(), recipe.cookingTime());
        bukkit.setGroup(recipe.group());
        bukkit.setCategory(CraftRecipe.getCategory(recipe.category()));
        return bukkit;
    }
}
