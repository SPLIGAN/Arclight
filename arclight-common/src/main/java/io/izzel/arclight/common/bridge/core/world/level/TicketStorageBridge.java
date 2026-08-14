package io.izzel.arclight.common.bridge.core.world.level;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.Ticket;

import java.util.List;

public interface TicketStorageBridge {

    Long2ObjectOpenHashMap<List<Ticket>> bridge$getTickets();
}
