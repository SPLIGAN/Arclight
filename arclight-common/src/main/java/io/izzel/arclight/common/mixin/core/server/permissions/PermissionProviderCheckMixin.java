package io.izzel.arclight.common.mixin.core.server.permissions;

import com.mojang.brigadier.tree.CommandNode;
import io.izzel.arclight.common.bridge.core.commands.CommandSourceStackBridge;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionProviderCheck;
import net.minecraft.server.permissions.PermissionSetSupplier;
import net.minecraft.server.permissions.Permissions;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PermissionProviderCheck.class)
public class PermissionProviderCheckMixin<T extends PermissionSetSupplier> {

    @Shadow @Final private PermissionCheck test;

    @Inject(method = "test(Lnet/minecraft/server/permissions/PermissionSetSupplier;)Z", cancellable = true, at = @At("HEAD"))
    private void arclight$bukkitPermission(T supplier, CallbackInfoReturnable<Boolean> cir) {
        if (!(supplier instanceof CommandSourceStackBridge bridge)) {
            return;
        }
        CommandNode<?> currentCommand = bridge.bridge$getCurrentCommand();
        if (currentCommand == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        CommandNode<net.minecraft.commands.CommandSourceStack> node =
                (CommandNode<net.minecraft.commands.CommandSourceStack>) currentCommand;
        cir.setReturnValue(bridge.bridge$hasPermission(arclight$requiredLevel(this.test), VanillaCommandWrapper.getPermission(node)));
    }

    private static int arclight$requiredLevel(PermissionCheck check) {
        if (check instanceof PermissionCheck.Require require) {
            Permission permission = require.permission();
            if (permission instanceof Permission.HasCommandLevel hasLevel) {
                return hasLevel.level().id();
            }
            if (permission.equals(Permissions.COMMANDS_ENTITY_SELECTORS)) {
                return PermissionLevel.GAMEMASTERS.id();
            }
        }
        return 0;
    }
}
