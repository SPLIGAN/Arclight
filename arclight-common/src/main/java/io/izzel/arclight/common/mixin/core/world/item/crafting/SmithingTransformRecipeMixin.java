package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftSmithingTransformRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin implements RecipeBridge {

    @Shadow @Final private ItemStackTemplate result;

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        SmithingTransformRecipe recipe = (SmithingTransformRecipe) (Object) this;
        if (this.result.count() == 0) {
            return new ArclightSpecialRecipe(id, recipe);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result.create());
        return new CraftSmithingTransformRecipe(
            id,
            result,
            recipe.templateIngredient().map(CraftRecipe::toBukkit).orElse(null),
            CraftRecipe.toBukkit(recipe.baseIngredient()),
            recipe.additionIngredient().map(CraftRecipe::toBukkit).orElse(null)
        );
    }
}
