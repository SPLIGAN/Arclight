package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import io.izzel.arclight.common.bridge.core.world.item.crafting.SingleItemRecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BlastingRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftBlastingRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlastingRecipe.class)
public abstract class BlastingRecipeMixin implements RecipeBridge {

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        BlastingRecipe recipe = (BlastingRecipe) (Object) this;
        ItemStackTemplate result = ((SingleItemRecipeBridge) recipe).bridge$result();
        if (result.count() == 0) {
            return new ArclightSpecialRecipe(id, recipe);
        }
        CraftBlastingRecipe bukkit = new CraftBlastingRecipe(id, CraftItemStack.asCraftMirror(result.create()), CraftRecipe.toBukkit(recipe.input()), recipe.experience(), recipe.cookingTime());
        bukkit.setGroup(recipe.group());
        bukkit.setCategory(CraftRecipe.getCategory(recipe.category()));
        return bukkit;
    }
}
