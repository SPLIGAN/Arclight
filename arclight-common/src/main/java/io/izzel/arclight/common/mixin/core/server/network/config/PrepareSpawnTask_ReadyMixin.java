package io.izzel.arclight.common.mixin.core.server.network.config;

import com.mojang.authlib.GameProfile;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * CraftBukkit reuses the ServerPlayer created during login.
 * Vanilla/NeoForge 26.1 PrepareSpawnTask creates a new one; reuse the captured login player instead.
 */
@Mixin(targets = "net.minecraft.server.network.config.PrepareSpawnTask$Ready")
public class PrepareSpawnTask_ReadyMixin {

    @Redirect(method = "spawn", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer arclight$reuseLoginPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation information) {
        ServerPlayer captured = ArclightCaptures.getLoginPlayer();
        if (captured != null) {
            return captured;
        }
        return new ServerPlayer(server, level, profile, information);
    }
}
