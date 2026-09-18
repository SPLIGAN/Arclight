package io.izzel.arclight.common.mixin.core.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.izzel.arclight.common.bridge.core.network.ConnectionBridge;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.server.network.ServerCommonPacketListenerImplBridge;
import io.izzel.arclight.common.bridge.core.server.network.ServerLoginPacketListenerImplBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.server.players.PlayerListBridge;
import io.izzel.arclight.common.mod.util.VelocitySupport;
import net.minecraft.util.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements ServerLoginPacketListenerImplBridge, CraftPlayer.TransferCookieConnection {

    // @formatter:off
    @Shadow private ServerLoginPacketListenerImpl.State state;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public Connection connection;
    @Shadow @Final private static AtomicInteger UNIQUE_THREAD_ID;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract void disconnect(Component reason);
    @Shadow public abstract String getUserName();
    @Shadow @Final private byte[] challenge;
    @Shadow @Nullable private String requestedUsername;
    @Shadow abstract void startClientVerification(GameProfile p_301095_);
    @Shadow protected abstract boolean isPlayerAlreadyInWorld(GameProfile p_298499_);
    @Shadow @Nullable private GameProfile authenticatedProfile;
    @Shadow @Final private boolean transferred;
    // @formatter:on

    private static final java.util.regex.Pattern PROP_PATTERN = java.util.regex.Pattern.compile("\\w{0,16}");

    private ServerPlayer player;
    @Unique protected int arclight$velocityLoginId = -1;

    @Override
    public int bridge$getVelocityLoginId() {
        return arclight$velocityLoginId;
    }

    @Override
    public void bridge$disconnect(String s) {
        this.disconnect(Component.literal(s));
    }

    public void disconnect(final String s) {
        bridge$disconnect(s);
    }

    // 26.1 dropped the offline-auth Thread; intercept createOfflineProfile instead.
    @Inject(method = "handleHello", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/UUIDUtil;createOfflineProfile(Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;"))
    private void arclight$velocityHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        if ((!this.server.usesAuthentication() || this.connection.isMemoryConnection()) && VelocitySupport.isEnabled()) {
            this.arclight$velocityLoginId = ThreadLocalRandom.current().nextInt();
            this.connection.send(new ClientboundCustomQueryPacket(this.arclight$velocityLoginId, VelocitySupport.createPacket()));
            ci.cancel();
        }
    }

    private static GameProfile arclight$createOfflineProfile(Connection connection, String name) {
        UUID uuid;
        if (((ConnectionBridge) connection).bridge$getSpoofedUUID() != null) {
            uuid = ((ConnectionBridge) connection).bridge$getSpoofedUUID();
        } else {
            uuid = UUIDUtil.createOfflinePlayerUUID(name);
        }

        GameProfile gameProfile = new GameProfile(uuid, name);

        if (((ConnectionBridge) connection).bridge$getSpoofedProfile() != null) {
            Property[] spoofedProfile;
            for (int length = (spoofedProfile = ((ConnectionBridge) connection).bridge$getSpoofedProfile()).length, i = 0; i < length; ++i) {
                final Property property = spoofedProfile[i];
                if (!PROP_PATTERN.matcher(property.name()).matches()) continue;
                gameProfile.properties().put(property.name(), property);
            }
        }
        return gameProfile;
    }

    @Redirect(method = "verifyLoginAndFinishConnectionSetup", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"))
    private Component arclight$canLogin(PlayerList instance, SocketAddress socketAddress, NameAndId nameAndId) {
        if (this.player == null) {
            this.player = ((PlayerListBridge) instance).bridge$canPlayerLogin(socketAddress, Objects.requireNonNull(this.authenticatedProfile), (ServerLoginPacketListenerImpl) (Object) this);
        }
        return null;
    }

    @Inject(method = "verifyLoginAndFinishConnectionSetup", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"))
    private void arclight$returnIfFail(GameProfile profile, CallbackInfo ci) {
        if (this.player == null) {
            ci.cancel();
        } else if (((ServerPlayerBridge) this.player).bridge$getBukkitEntity().isAwaitingCookies()) {
            // Stay in VERIFYING; tick will retry until cookies are complete (no WAITING_FOR_COOKIES on vanilla).
            ci.cancel();
        }
    }

    @Redirect(method = "verifyLoginAndFinishConnectionSetup", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;disconnectAllPlayersWithProfile(Ljava/util/UUID;)Z"))
    private boolean arclight$skipKick(PlayerList instance, UUID uuid) {
        return this.isPlayerAlreadyInWorld(Objects.requireNonNull(this.authenticatedProfile));
    }

    @Inject(method = "handleLoginAcknowledgement", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void arclight$setPlayer(ServerboundLoginAcknowledgedPacket p_298815_, CallbackInfo ci, CommonListenerCookie cookie, ServerConfigurationPacketListenerImpl listener) {
        ((ServerCommonPacketListenerImplBridge) listener).bridge$setPlayer(this.player);
    }

    @Inject(method = "handleCookieResponse", cancellable = true, at = @At("HEAD"))
    private void arclight$cookieResponse(ServerboundCookieResponsePacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerLoginPacketListenerImpl) (Object) this, this.server.packetProcessor());
        if (this.player != null && ((ServerPlayerBridge) this.player).bridge$getBukkitEntity().handleCookieResponse(packet)) {
            ci.cancel();
        }
    }

    @Unique
    private void arclight$callPlayerPreLoginEvents(GameProfile profile) throws Exception {
        String playerName = profile.name();
        InetAddress address = ((InetSocketAddress) this.connection.getRemoteAddress()).getAddress();
        UUID uniqueId = profile.id();
        CraftServer craftServer = ((MinecraftServerBridge) this.server).bridge$getServer();

        AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId, this.transferred);
        craftServer.getPluginManager().callEvent(asyncEvent);

        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
            if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
            }
            Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<>() {
                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    craftServer.getPluginManager().callEvent(event);
                    return event.getResult();
                }
            };
            ((MinecraftServerBridge) this.server).bridge$queuedProcess(waitable);
            if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                this.disconnect(event.getKickMessage());
            }
        } else if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            this.disconnect(asyncEvent.getKickMessage());
        }
    }

    @Inject(method = "startClientVerification", at = @At("HEAD"), cancellable = true)
    private void arclight$preLoginEvents(GameProfile profile, CallbackInfo ci) {
        // Vanilla has no CraftBukkit callPlayerPreLoginEvents; fire Bukkit events here for all auth paths.
        // Velocity already calls arclight$callPlayerPreLoginEvents before startClientVerification — skip duplicate.
        if (this.arclight$velocityLoginId != -1 && VelocitySupport.isEnabled()) {
            return;
        }
        try {
            this.arclight$callPlayerPreLoginEvents(profile);
        } catch (Exception ex) {
            this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
            LOGGER.warn("Exception verifying {}", profile.name(), ex);
            ci.cancel();
        }
    }

    /*

     * Forgified Fabric API (FFAPI) will actively record every custom query and awaits all responses
     * before we enter the configuration stage. Due to their powerful control on queries we must allow
     * them to at least have a glance on what they receive.
     * FFAPI selected its injection point at the HEAD of this method. Thus, we selected INVOKE disconnect
     * to ensure a defined injection order.
     * Due to lack of support on custom queries in Forge/NF, FFAPI aggressively deserialize all CustomQA
     * payload into its own kind; it is thus needed to take special care when processing the payload.
     * Fallback implementation will log a loud warning and try to serialize the custom payload to recreate
     * original answer data. This does not work for FFAPI since their payload is a buffer wrapper and has
     * consumed the buffer by the end of their handler.
     * See Forge/NF CustomQA deserialization & ArclightCustomQueryAnswerPayload.
     * See FFAPI compat impl for customQAData & onCustomQA.
     */
    @Inject(method = "handleCustomQueryPacket", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private void arclight$modernForwardReply(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if (VelocitySupport.isEnabled() && packet.transactionId() == this.bridge$getVelocityLoginId()) {
            var payload = arclight$platform$customQAData(packet);
            if (payload == null) {
                this.bridge$disconnect("This server requires you to connect with Velocity.");
                ci.cancel();
                return;
            }
            var buf = payload.readNullable(r -> {
                int i = r.readableBytes();
                if (i >= 0 && i <= 1048576) {
                    return new FriendlyByteBuf(r.readBytes(i));
                } else {
                    throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
                }
            });
            if (buf == null) {
                this.bridge$disconnect("This server requires you to connect with Velocity.");
                ci.cancel();
                return;
            }

            if (!VelocitySupport.checkIntegrity(buf)) {
                this.bridge$disconnect("Unable to verify player details");
                ci.cancel();
                return;
            }

            int version = buf.readVarInt();
            if (version > VelocitySupport.MAX_SUPPORTED_FORWARDING_VERSION) {
                throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted upto " + VelocitySupport.MAX_SUPPORTED_FORWARDING_VERSION);
            }
            java.net.SocketAddress listening = this.connection.getRemoteAddress();
            int port = 0;
            if (listening instanceof java.net.InetSocketAddress) {
                port = ((java.net.InetSocketAddress) listening).getPort();
            }
            this.connection.address = new java.net.InetSocketAddress(VelocitySupport.readAddress(buf), port);
            this.authenticatedProfile = VelocitySupport.createProfile(buf);

            // Proceed with login
            Util.backgroundExecutor().execute(() -> {
                try {
                    if (this.arclight$velocityLoginId == -1 && VelocitySupport.isEnabled()) {
                        disconnect("This server requires you to connect with Velocity.");
                        return;
                    }
                    this.arclight$callPlayerPreLoginEvents(this.authenticatedProfile);
                    LOGGER.info("UUID of player {} is {}", this.authenticatedProfile.name(), this.authenticatedProfile.id());
                    this.startClientVerification(this.authenticatedProfile);
                } catch (Exception ex) {
                    disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                    LOGGER.warn("Exception verifying {} ", this.authenticatedProfile.name(), ex);
                }
            });
            this.arclight$platform$onCustomQA(packet);
            ci.cancel();
        }
    }

}
