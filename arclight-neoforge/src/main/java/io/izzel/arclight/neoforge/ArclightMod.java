package io.izzel.arclight.neoforge;

import io.izzel.arclight.api.Arclight;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.neoforge.mod.NeoForgeArclightServer;
import io.izzel.arclight.neoforge.mod.event.ArclightEventDispatcherRegistry;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;

@Mod("arclight")
public class ArclightMod {

    public ArclightMod() {
        ArclightServer.LOGGER.info("mod-load");
        Arclight.setServer(new NeoForgeArclightServer());
        System.setOut(new LoggingPrintStream("STDOUT", System.out, Level.INFO));
        System.setErr(new LoggingPrintStream("STDERR", System.err, Level.ERROR));
        ArclightEventDispatcherRegistry.registerAllEventDispatchers();
        // Force transform login-critical classes at boot so MixinTransformerError surfaces
        // in the server log with a full stack instead of only as a client disconnect reason.
        forceLoadLoginClasses();
    }

    private static void forceLoadLoginClasses() {
        String[] names = {
            "net.minecraft.world.entity.Avatar",
            "net.minecraft.world.entity.player.Player",
            "net.minecraft.world.entity.LivingEntity",
            "net.minecraft.server.level.ServerPlayer",
            "net.minecraft.server.level.PlayerSpawnFinder",
            "net.minecraft.server.network.ServerHandshakePacketListenerImpl",
            "net.minecraft.server.network.ServerLoginPacketListenerImpl",
            "net.minecraft.server.network.ServerCommonPacketListenerImpl",
            "net.minecraft.server.network.ServerConfigurationPacketListenerImpl",
            "net.minecraft.server.network.config.PrepareSpawnTask",
            "net.minecraft.server.network.config.PrepareSpawnTask$Ready",
            "net.minecraft.stats.ServerRecipeBook",
            "net.minecraft.network.protocol.PacketUtils",
            "net.minecraft.server.level.ServerEntity",
            "net.minecraft.server.network.ServerGamePacketListenerImpl",
            // Deferred-load hotspots that previously crashed mid-session after a successful join
            "net.minecraft.world.level.storage.SavedDataStorage",
            "net.minecraft.world.level.block.entity.HopperBlockEntity",
            "net.neoforged.neoforge.transfer.item.VanillaInventoryCodeHooks",
            "org.bukkit.craftbukkit.block.CraftChest",
            "io.izzel.arclight.common.mod.server.block.ChestBlockDoubleInventoryHacks",
            // Villager AI loads during chunk gen / village spawn after join
            "net.minecraft.world.entity.npc.villager.Villager",
            "net.minecraft.world.entity.ai.behavior.HarvestFarmland",
            "net.minecraft.world.entity.ai.behavior.VillagerMakeLove",
            // InventoryHolder cleanup on despawn (Craft field access needs AW/AT)
            "net.minecraft.world.entity.animal.nautilus.AbstractNautilus",
            "org.bukkit.craftbukkit.entity.CraftAbstractNautilus",
            "net.minecraft.world.entity.monster.illager.Pillager",
            "org.bukkit.craftbukkit.entity.CraftPillager",
            // Lava/fire portal creation loads PortalShape after join — force-apply mixin at boot
            "net.minecraft.world.level.portal.PortalShape",
            "net.minecraft.world.level.block.BaseFireBlock"
        };
        for (String name : names) {
            try {
                Class.forName(name, false, ArclightMod.class.getClassLoader());
                ArclightServer.LOGGER.info("login-class-ok {}", name);
            } catch (Throwable t) {
                ArclightServer.LOGGER.fatal("login-class-FAIL " + name, t);
                // Do not continue with a half-broken server; deferred-load crashes after join are worse.
                throw new ExceptionInInitializerError(t);
            }
        }
    }

    private static class LoggingPrintStream extends PrintStream {

        private final Logger logger;
        private final Level level;

        public LoggingPrintStream(String name, @NotNull OutputStream out, Level level) {
            super(out);
            this.logger = LogManager.getLogger(name);
            this.level = level;
        }

        @Override
        public void println(@Nullable String x) {
            logger.log(level, x);
        }

        @Override
        public void println(@Nullable Object x) {
            logger.log(level, String.valueOf(x));
        }
    }
}
