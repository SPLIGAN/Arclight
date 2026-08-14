package io.izzel.arclight.common.mixin.core.server.level;

import io.izzel.arclight.common.bridge.core.server.level.TicketTypeBridge;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TicketType.class)
public abstract class TicketTypeMixin implements TicketTypeBridge {

    // CraftBukkit: FLAG_LOADING | FLAG_SIMULATION | FLAG_KEEP_DIMENSION_ACTIVE (= 14)
    private static final int PLUGIN_FLAGS = TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE;

    /**
     * CraftBukkit stores chunk-gc period here; CraftServer assigns it from bukkit.yml.
     * PLUGIN.timeout() reads this instead of the record component.
     */
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static long pluginTimeout = 0L;

    // Record targets reject non-private static fields; TransformAccess widens after mixin apply.
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)
    private static final TicketType PLUGIN = registerPlugin("plugin", PLUGIN_FLAGS);

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)
    private static final TicketType PLUGIN_TICKET = registerPlugin("plugin_ticket", PLUGIN_FLAGS);

    private static TicketType registerPlugin(String name, int flags) {
        return Registry.register(BuiltInRegistries.TICKET_TYPE, name, new TicketType(TicketType.NO_TIMEOUT, flags));
    }

    @Inject(method = "timeout", at = @At("HEAD"), cancellable = true)
    private void arclight$pluginTimeout(CallbackInfoReturnable<Long> cir) {
        if ((Object) this == PLUGIN) {
            cir.setReturnValue(pluginTimeout);
        }
    }

    @Override
    public void bridge$setLifespan(long lifespan) {
        pluginTimeout = lifespan;
    }
}
