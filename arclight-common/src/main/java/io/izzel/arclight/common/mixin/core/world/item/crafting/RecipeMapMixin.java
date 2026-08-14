package io.izzel.arclight.common.mixin.core.world.item.crafting;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeMapBridge;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin implements RecipeMapBridge {

    @Shadow @Mutable private Multimap<RecipeType<?>, RecipeHolder<?>> byType;
    @Shadow @Final @Mutable private Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

    @Shadow public abstract <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type);

    @Override
    public void bridge$makeMutable() {
        this.byType = LinkedHashMultimap.create(this.byType);
        this.byKey = new HashMap<>(this.byKey);
    }

    @Override
    public void bridge$addRecipe(RecipeHolder<?> recipe) {
        bridge$makeMutable();
        if (this.byKey.containsKey(recipe.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.id());
        }
        this.byType.put(recipe.value().getType(), recipe);
        this.byKey.put(recipe.id(), recipe);
    }

    @Override
    public boolean bridge$removeRecipe(ResourceKey<Recipe<?>> key) {
        bridge$makeMutable();
        RecipeHolder<?> removed = this.byKey.remove(key);
        if (removed != null) {
            this.byType.values().removeIf(holder -> holder.id().equals(key));
            return true;
        }
        return false;
    }

    @Override
    public void bridge$clear() {
        this.byType = LinkedHashMultimap.create();
        this.byKey = new HashMap<>();
    }

    @Override
    public <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> bridge$byType(RecipeType<T> type) {
        return this.byType(type);
    }
}
