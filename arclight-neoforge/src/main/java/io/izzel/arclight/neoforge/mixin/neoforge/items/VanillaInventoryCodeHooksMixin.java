package io.izzel.arclight.neoforge.mixin.neoforge.items;

import io.izzel.arclight.common.bridge.core.world.IInventoryBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.neoforge.mod.util.DelegatedContainer;
import io.izzel.arclight.neoforge.mod.util.HopperTransferContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ContainerOrHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaInventoryCodeHooks;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge 26.1 {@link VanillaInventoryCodeHooks} only exposes hopper
 * {@code insertHook}/{@code extractHook}/{@code getEntityContainerOrHandler}.
 * There is no {@code dropperInsertHook}; dispenser→container moves stay on
 * vanilla {@code DispenserBlock}/{@code DispenserBlockEntity} Bukkit hooks.
 */
@Mixin(VanillaInventoryCodeHooks.class)
public abstract class VanillaInventoryCodeHooksMixin {

    @Inject(method = "getEntityContainerOrHandler", at = @At("RETURN"), remap = false)
    private static void arclight$captureEntityHandler(Level level, double x, double y, double z, Direction side, CallbackInfoReturnable<ContainerOrHandler> cir) {
        ContainerOrHandler result = cir.getReturnValue();
        if (result != null && !result.isEmpty() && result.itemHandler() != null) {
            DelegatedContainer.recordLastHandler();
            HopperTransferContext.push(result);
        }
    }

    // 26.1: ResourceHandler.insert/extract take Resource (generic bound), not ItemResource in the descriptor.
    // Decorate arg order: invoke receiver + invoke args, then enclosing-method args (hopper/dest).
    @Decorate(method = "insertHook", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/transfer/ResourceHandler;insert(Lnet/neoforged/neoforge/transfer/resource/Resource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I"), remap = false)
    private static int arclight$sourceInitiatedMoveItem(ResourceHandler<?> handler, Resource resource, int amount, TransactionContext tx, HopperBlockEntity hopper) throws Throwable {
        try {
            ItemResource itemResource = (ItemResource) resource;
            ItemStack insertStack = itemResource.toStack(amount);
            if (!insertStack.isEmpty()) {
                CraftItemStack craftItemStack = CraftItemStack.asCraftMirror(insertStack);
                Inventory destInventory = HopperTransferContext.toInventory(HopperTransferContext.peek());
                InventoryMoveItemEvent event = new InventoryMoveItemEvent(((IInventoryBridge) hopper).getOwnerInventory(), craftItemStack.clone(), destInventory, true);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    hopper.setCooldown(((WorldBridge) hopper.getLevel()).bridge$spigotConfig().hopperTransfer);
                    return 0;
                }
                resource = ItemResource.of(CraftItemStack.asNMSCopy(event.getItem()));
            }
            return (int) DecorationOps.callsite().invoke(handler, resource, amount, tx);
        } finally {
            HopperTransferContext.clear();
        }
    }

    @Decorate(method = "extractHook", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/transfer/ResourceHandler;extract(ILnet/neoforged/neoforge/transfer/resource/Resource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I"), remap = false)
    private static int arclight$nonSourceInitiatedMoveItem(ResourceHandler<?> handler, int index, Resource resource, int amount, TransactionContext tx, Hopper dest) throws Throwable {
        try {
            ItemResource itemResource = (ItemResource) resource;
            ItemStack preview = itemResource.toStack(amount);
            if (!preview.isEmpty()) {
                CraftItemStack original = CraftItemStack.asCraftMirror(preview);
                Inventory sourceInventory = HopperTransferContext.toInventory(HopperTransferContext.peek());
                InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, original.clone(), ((IInventoryBridge) dest).getOwnerInventory(), false);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    if (dest instanceof HopperBlockEntity entity) {
                        entity.setCooldown(((WorldBridge) entity.getLevel()).bridge$spigotConfig().hopperTransfer);
                    }
                    return 0;
                }
                preview = CraftItemStack.asNMSCopy(event.getItem());
                resource = ItemResource.of(preview);
            }
            return (int) DecorationOps.callsite().invoke(handler, index, resource, amount, tx);
        } finally {
            HopperTransferContext.clear();
        }
    }
}
