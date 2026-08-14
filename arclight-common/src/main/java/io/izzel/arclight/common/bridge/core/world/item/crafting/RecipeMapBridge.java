package io.izzel.arclight.common.bridge.core.world.item.crafting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;

public interface RecipeMapBridge {

    void bridge$makeMutable();

    void bridge$addRecipe(RecipeHolder<?> recipe);

    boolean bridge$removeRecipe(ResourceKey<Recipe<?>> key);

    void bridge$clear();

    <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> bridge$byType(RecipeType<T> type);
}
