package io.izzel.arclight.common.mixin.core.server.level;

import io.izzel.arclight.common.bridge.core.server.level.TicketBridge;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

// 26.1: Ticket no longer overrides equals/hashCode; plugin keys are matched explicitly (see DistanceManagerMixin).
@Mixin(Ticket.class)
public abstract class TicketMixin implements TicketBridge {

    // CraftBukkit field name; TicketStorage compares keys directly.
    public Object key;

    // Declared private for mixin apply; TransformAccess widens to public static for CraftBukkit.
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static Ticket of(TicketType type, int level, Object key) {
        Ticket ticket = new Ticket(type, level);
        ((TicketBridge) (Object) ticket).bridge$setKey(key);
        return ticket;
    }

    @Override
    public Object bridge$getKey() {
        return this.key;
    }

    @Override
    public void bridge$setKey(Object key) {
        this.key = key;
    }
}
