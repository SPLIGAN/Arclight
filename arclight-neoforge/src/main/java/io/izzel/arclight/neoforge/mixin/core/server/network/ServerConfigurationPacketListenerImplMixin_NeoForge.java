package io.izzel.arclight.neoforge.mixin.core.server.network;

import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin_NeoForge extends ServerCommonPacketListenerImplMixin_NeoForge {

    // @formatter:off
    @Shadow protected abstract void runConfiguration();
    // @formatter:on

    // PlayerLinksSendEvent is handled once in common ServerConfigurationPacketListenerImplMixin
    // (duplicate @Decorate here broke apply: stack type mismatch on serverLinks()).

    @Redirect(method = "handlePong", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;runConfiguration()V"))
    private void arclight$runConfigurationMainThread(ServerConfigurationPacketListenerImpl instance) {
        if (ArclightServer.isPrimaryThread()) {
            this.runConfiguration();
        } else {
            ArclightServer.executeOnMainThread(this::runConfiguration);
        }
    }
}
