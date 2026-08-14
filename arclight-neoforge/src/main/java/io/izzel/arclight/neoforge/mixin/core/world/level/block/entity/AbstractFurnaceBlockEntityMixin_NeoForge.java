package io.izzel.arclight.neoforge.mixin.core.world.level.block.entity;

import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin_NeoForge {

    // 26.1: burn is void (no boolean return); fire FurnaceSmeltEvent at HEAD and cancel the body when needed.
    @Decorate(method = "burn", inject = true, at = @At("HEAD"))
    private static void arclight$furnaceSmelt(NonNullList<ItemStack> items, ItemStack itemStack1, ItemStack itemStack2) throws Throwable {
        BlockEntity blockEntity = ArclightCaptures.getTickingBlockEntity();
        if (blockEntity == null) {
            return;
        }
        CraftItemStack source = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemStack2);

        FurnaceSmeltEvent furnaceSmeltEvent = new FurnaceSmeltEvent(CraftBlock.at(blockEntity.getLevel(), blockEntity.getBlockPos()), source, result);
        Bukkit.getPluginManager().callEvent(furnaceSmeltEvent);

        if (furnaceSmeltEvent.isCancelled()) {
            DecorationOps.cancel().invoke();
            return;
        }

        result = furnaceSmeltEvent.getResult();
        ItemStack nmsResult = CraftItemStack.asNMSCopy(result);
        if (nmsResult.isEmpty()) {
            itemStack1.shrink(1);
            DecorationOps.cancel().invoke();
            return;
        }
        // Replace the result argument for the rest of burn().
        itemStack2 = nmsResult;
        DecorationOps.blackhole().invoke(itemStack2);
    }
}
