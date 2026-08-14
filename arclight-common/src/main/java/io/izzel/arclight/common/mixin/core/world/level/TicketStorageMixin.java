package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.world.level.TicketStorageBridge;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TicketStorage.class)
public abstract class TicketStorageMixin implements TicketStorageBridge {

    @Accessor("tickets")
    public abstract Long2ObjectOpenHashMap<List<Ticket>> bridge$getTickets();
}
