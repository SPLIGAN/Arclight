package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.SingleItemRecipeBridge;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SingleItemRecipe.class)
public class SingleItemRecipeMixin implements SingleItemRecipeBridge {

    @Shadow @Final private ItemStackTemplate result;

    @Override
    public ItemStackTemplate bridge$result() {
        return this.result;
    }
}
