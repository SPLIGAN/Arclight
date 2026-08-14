package io.izzel.arclight.common.mixin.core.world.entity.npc;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.item.trading.MerchantBridge;
import io.izzel.arclight.common.bridge.core.world.IInventoryBridge;
import io.izzel.arclight.common.bridge.core.world.item.trading.MerchantOfferBridge;
import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractVillager;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.npc.villager.AbstractVillager.class)
public abstract class AbstractVillagerMixin extends PathfinderMobMixin implements MerchantBridge {

    @Shadow @Final private SimpleContainer inventory;

    @Unique
    private static final ThreadLocal<net.minecraft.world.entity.npc.villager.AbstractVillager> ARCLIGHT$OFFER_OWNER = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$init(EntityType<? extends net.minecraft.world.entity.npc.villager.AbstractVillager> type, Level worldIn, CallbackInfo ci) {
        ((IInventoryBridge) this.inventory).setOwner((InventoryHolder) this.getBukkitEntity());
    }

    private CraftMerchant craftMerchant;

    @Override
    public CraftMerchant bridge$getCraftMerchant() {
        return (craftMerchant == null) ? craftMerchant = new CraftAbstractVillager(((CraftServer) Bukkit.getServer()), (net.minecraft.world.entity.npc.villager.AbstractVillager) (Object) this) : craftMerchant;
    }

    @Inject(method = "addOffersFromTradeSet", at = @At("HEAD"))
    private void arclight$captureOfferOwner(ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> tradeSet, CallbackInfo ci) {
        ARCLIGHT$OFFER_OWNER.set((net.minecraft.world.entity.npc.villager.AbstractVillager) (Object) this);
    }

    @Inject(method = "addOffersFromTradeSet", at = @At("RETURN"))
    private void arclight$clearOfferOwner(ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> tradeSet, CallbackInfo ci) {
        ARCLIGHT$OFFER_OWNER.remove();
    }

    @Redirect(method = {"addOffersFromItemListings", "addOffersFromItemListingsWithoutDuplicates"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z"))
    private static boolean arclight$gainOffer(MerchantOffers merchantOffers, Object e) {
        MerchantOffer offer = (MerchantOffer) e;
        net.minecraft.world.entity.npc.villager.AbstractVillager owner = ARCLIGHT$OFFER_OWNER.get();
        if (owner == null) {
            return merchantOffers.add(offer);
        }
        VillagerAcquireTradeEvent event = new VillagerAcquireTradeEvent(
            (AbstractVillager) ((EntityBridge) owner).bridge$getBukkitEntity(),
            ((MerchantOfferBridge) offer).bridge$asBukkit());
        if (((EntityBridge) owner).bridge$isValid()) {
            Bukkit.getPluginManager().callEvent(event);
        }
        if (!event.isCancelled()) {
            return merchantOffers.add(CraftMerchantRecipe.fromBukkit(event.getRecipe()).toMinecraft());
        }
        return false;
    }
}
