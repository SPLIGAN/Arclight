package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin implements RecipeBridge {

    // @formatter:off
    @Shadow @Final ItemStackTemplate result;
    @Shadow @Final List<Ingredient> ingredients;
    // @formatter:on

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        NormalCraftingRecipe self = (NormalCraftingRecipe) (Object) this;
        if (this.result.count() == 0) {
            return new ArclightSpecialRecipe(id, (ShapelessRecipe) (Object) this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result.create());
        CraftShapelessRecipe recipe = new CraftShapelessRecipe(id, result, (ShapelessRecipe) (Object) this);
        recipe.setGroup(self.group());
        recipe.setCategory(CraftRecipe.getCategory(self.category()));
        for (Ingredient list : this.ingredients) {
            recipe.addIngredient(CraftRecipe.toBukkit(list));
        }
        return recipe;
    }
}
