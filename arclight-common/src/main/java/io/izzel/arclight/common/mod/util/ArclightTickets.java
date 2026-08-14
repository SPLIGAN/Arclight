package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.server.level.TicketBridge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;

public final class ArclightTickets {

    private ArclightTickets() {
    }

    public static TicketType pluginChunkTicket() {
        return BuiltInRegistries.TICKET_TYPE.getValue(Identifier.withDefaultNamespace("plugin_ticket"));
    }

    public static Ticket create(TicketType type, int level, Object key) {
        Ticket ticket = new Ticket(type, level);
        ((TicketBridge) (Object) ticket).bridge$setKey(key);
        return ticket;
    }
}
