package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.izzel.arclight.common.bridge.core.server.level.ServerLevelBridge;
import io.izzel.arclight.common.bridge.core.server.level.TicketBridge;
import io.izzel.arclight.common.bridge.core.world.level.TicketStorageBridge;
import io.izzel.arclight.common.bridge.core.world.server.ServerChunkProviderBridge;
import io.izzel.arclight.common.mod.util.ArclightTickets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.gamerules.GameRules;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.craftbukkit.CraftGameRule;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(CraftWorld.class)
public abstract class CraftWorldMixin {

    // @formatter:off
    @Shadow @Final private ServerLevel world;
    @Shadow public abstract Chunk getChunkAt(int x, int z);
    // @formatter:on

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite(remap = false)
    public File getWorldFolder() {
        return ((ServerLevelBridge) this.world).bridge$getConvertable().getDimensionPath(this.world.dimension()).toFile();
    }

    /**
     * @author IzzelAliz
     * @reason CraftBukkit 26.1 calls {@code GameRules.set(..., ServerLevel)}; NeoForge only has {@code MinecraftServer}.
     */
    @Overwrite(remap = false)
    public void setPVP(boolean pvp) {
        this.world.getGameRules().set(GameRules.PVP, pvp, this.world.getServer());
    }

    /**
     * @author IzzelAliz
     * @reason Same {@code GameRules.set} signature mismatch as {@link #setPVP(boolean)}.
     */
    @Overwrite(remap = false)
    public <T> boolean setGameRule(GameRule<T> rule, T newValue) {
        Preconditions.checkArgument(rule != null, "GameRule cannot be null");
        Preconditions.checkArgument(newValue != null, "GameRule value cannot be null");
        if (!rule.getType().equals(newValue.getClass())) {
            return false;
        }
        net.minecraft.world.level.gamerules.GameRule<T> nms = CraftGameRule.bukkitToMinecraft(rule);
        this.world.getGameRules().set(nms, newValue, this.world.getServer());
        return true;
    }

    private TicketStorage arclight$ticketStorage() {
        return ((ServerChunkProviderBridge) this.world.getChunkSource()).bridge$getTicketStorage();
    }

    /**
     * @author IzzelAliz
     * @reason NeoForge keeps ServerChunkCache.ticketStorage private; CraftBukkit cannot GETFIELD it across modules.
     */
    @Overwrite(remap = false)
    public boolean addPluginChunkTicket(int x, int z, Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "null plugin");
        Preconditions.checkArgument(plugin.isEnabled(), "plugin is not enabled");
        Ticket ticket = ArclightTickets.create(ArclightTickets.pluginChunkTicket(), ChunkMap.FORCED_TICKET_LEVEL, plugin);
        if (arclight$ticketStorage().addTicket(ChunkPos.pack(x, z), ticket)) {
            this.getChunkAt(x, z);
            return true;
        }
        return false;
    }

    /**
     * @author IzzelAliz
     * @reason NeoForge keeps ServerChunkCache.ticketStorage private.
     */
    @Overwrite(remap = false)
    public boolean removePluginChunkTicket(int x, int z, Plugin plugin) {
        Preconditions.checkNotNull(plugin, "null plugin");
        Ticket ticket = ArclightTickets.create(ArclightTickets.pluginChunkTicket(), ChunkMap.FORCED_TICKET_LEVEL, plugin);
        return arclight$ticketStorage().removeTicket(ChunkPos.pack(x, z), ticket);
    }

    /**
     * @author IzzelAliz
     * @reason NeoForge keeps ServerChunkCache.ticketStorage private.
     */
    @Overwrite(remap = false)
    public void removePluginChunkTickets(Plugin plugin) {
        Preconditions.checkNotNull(plugin, "null plugin");
        TicketType type = ArclightTickets.pluginChunkTicket();
        arclight$ticketStorage().removeTicketIf((ticket, i) ->
            ticket.getType() == type && Objects.equals(((TicketBridge) (Object) ticket).bridge$getKey(), plugin), null);
    }

    /**
     * @author IzzelAliz
     * @reason NeoForge keeps ServerChunkCache.ticketStorage private.
     */
    @Overwrite(remap = false)
    public Collection<Plugin> getPluginChunkTickets(int x, int z) {
        List<Ticket> tickets = arclight$ticketStorage().getTickets(ChunkPos.pack(x, z));
        if (tickets == null) {
            return Collections.emptyList();
        }
        TicketType type = ArclightTickets.pluginChunkTicket();
        ImmutableList.Builder<Plugin> ret = ImmutableList.builder();
        for (Ticket ticket : tickets) {
            if (ticket.getType() == type) {
                ret.add((Plugin) ((TicketBridge) (Object) ticket).bridge$getKey());
            }
        }
        return ret.build();
    }

    /**
     * @author IzzelAliz
     * @reason NeoForge keeps ServerChunkCache.ticketStorage and TicketStorage.tickets private.
     */
    @Overwrite(remap = false)
    public Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
        Map<Plugin, ImmutableList.Builder<Chunk>> ret = new HashMap<>();
        TicketType type = ArclightTickets.pluginChunkTicket();
        for (Long2ObjectMap.Entry<List<Ticket>> chunkTickets : ((TicketStorageBridge) arclight$ticketStorage()).bridge$getTickets().long2ObjectEntrySet()) {
            long chunkKey = chunkTickets.getLongKey();
            Chunk chunk = null;
            for (Ticket ticket : chunkTickets.getValue()) {
                if (ticket.getType() != type) {
                    continue;
                }
                if (chunk == null) {
                    chunk = this.getChunkAt(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey));
                }
                ret.computeIfAbsent((Plugin) ((TicketBridge) (Object) ticket).bridge$getKey(), key -> ImmutableList.builder()).add(chunk);
            }
        }
        return ret.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    }
}
