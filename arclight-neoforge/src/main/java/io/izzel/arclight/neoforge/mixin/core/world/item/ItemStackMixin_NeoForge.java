package io.izzel.arclight.neoforge.mixin.core.world.item;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.world.item.ItemStackBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_NeoForge implements ItemStackBridge, IItemStackExtension {

    // @formatter:off
    // 26.1: item is Holder<Item> (was Item).
    @Mutable @Shadow @Final private Holder<Item> item;
    // @formatter:on

    // 26.1: EnchantmentHelper durability call moved into processDurabilityChange.
    @Redirect(method = "processDurabilityChange(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"))
    private int arclight$itemDamage(ServerLevel serverLevel, ItemStack itemStack, int i, int amount, ServerLevel level, LivingEntity damager) {
        int result = EnchantmentHelper.processDurabilityChange(serverLevel, itemStack, i);
        if (damager instanceof ServerPlayer) {
            PlayerItemDamageEvent event = new PlayerItemDamageEvent(((ServerPlayerBridge) damager).bridge$getBukkitEntity(), CraftItemStack.asCraftMirror((ItemStack) (Object) this), result);
            event.getPlayer().getServer().getPluginManager().callEvent(event);

            if (result != event.getDamage() || event.isCancelled()) {
                event.getPlayer().updateInventory();
            }
            if (event.isCancelled()) {
                return 0;
            }
            result = event.getDamage();
        }
        return result;
    }

    // 26.1: break callback lives in applyDamage.
    @Inject(method = "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private void arclight$itemBreak(int amount, LivingEntity livingEntity, Consumer<Item> onBroken, CallbackInfo ci) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            CraftEventFactory.callPlayerItemBreakEvent(serverPlayer, (ItemStack) (Object) this);
        }
    }

    @Deprecated
    public void setItem(@Nullable Item item) {
        if (item == null) {
            return;
        }
        this.item = item.builtInRegistryHolder();
    }

    @Override
    public InteractionResult bridge$forge$onItemUseFirst(UseOnContext context) {
        return onItemUseFirst(context);
    }

    @Override
    public boolean bridge$forge$doesSneakBypassUse(LevelReader level, BlockPos pos, Player player) {
        return doesSneakBypassUse(level, pos, player);
    }
}
