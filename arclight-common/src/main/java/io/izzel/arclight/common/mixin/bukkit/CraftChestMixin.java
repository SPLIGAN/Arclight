package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spigot CraftChest references {@code ChestBlock.DoubleChestCombiner.Dummy.DoubleInventory},
 * which does not exist on NeoForge/vanilla 26.1. Replace getInventory with ChestBlock.getContainer.
 */
@Mixin(value = CraftChest.class, remap = false)
public abstract class CraftChestMixin {

    @Inject(method = "getInventory", at = @At("HEAD"), cancellable = true)
    private void arclight$getInventoryWithoutSpigotDoubleInventory(CallbackInfoReturnable<Inventory> cir) {
        CraftChest self = (CraftChest) (Object) this;
        CraftInventory inventory = (CraftInventory) self.getBlockInventory();
        if (!self.isPlaced()) {
            cir.setReturnValue(inventory);
            return;
        }

        LevelAccessor worldHandle = self.getWorldHandle();
        if (!(worldHandle instanceof Level)) {
            cir.setReturnValue(inventory);
            return;
        }

        CraftWorld world = (CraftWorld) self.getWorld();
        ChestBlock blockChest = (ChestBlock) self.getHandle().getBlock();
        Container container = ChestBlock.getContainer(blockChest, self.getHandle(), world.getHandle(), self.getPosition(), true);
        if (container instanceof CompoundContainer compoundContainer) {
            cir.setReturnValue(new CraftInventory(compoundContainer));
            return;
        }
        cir.setReturnValue(inventory);
    }
}
