package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeManagerBridge;
import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeMapBridge;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements RecipeManagerBridge {

    @Shadow private RecipeMap recipes;

    @Inject(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    private void arclight$makeMutable(RecipeMap recipeMap, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
        ((RecipeMapBridge) (Object) this.recipes).bridge$makeMutable();
    }

    /**
     * @author IzzelAliz
     * @reason CraftBukkit SPIGOT-4638: last matching recipe gets priority
     */
    @Overwrite
    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> type, I input, Level level, @Nullable RecipeHolder<T> lastRecipe) {
        List<RecipeHolder<T>> list = this.recipes.byType(type).stream().filter(holder -> holder.value().matches(input, level)).toList();
        return (list.isEmpty() || input.isEmpty())
            ? Optional.empty()
            : (lastRecipe != null && lastRecipe.value().matches(input, level) ? Optional.of(lastRecipe) : Optional.of(list.getLast()));
    }

    public void addRecipe(RecipeHolder<?> recipe) {
        ((RecipeMapBridge) (Object) this.recipes).bridge$addRecipe(recipe);
    }

    @Override
    public void bridge$addRecipe(RecipeHolder<?> recipe) {
        addRecipe(recipe);
    }

    public boolean removeRecipe(Identifier mcKey) {
        ResourceKey<Recipe<?>> toRemove = null;
        for (RecipeHolder<?> holder : this.recipes.values()) {
            if (holder.id().identifier().equals(mcKey)) {
                toRemove = holder.id();
                break;
            }
        }
        return toRemove != null && ((RecipeMapBridge) (Object) this.recipes).bridge$removeRecipe(toRemove);
    }

    public void clearRecipes() {
        ((RecipeMapBridge) (Object) this.recipes).bridge$clear();
    }

    @Override
    public void bridge$clearRecipes() {
        clearRecipes();
    }
}
