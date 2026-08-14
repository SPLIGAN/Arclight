package io.izzel.arclight.neoforge.mixin.core.server.level;

import io.izzel.arclight.common.bridge.core.server.level.ServerLevelBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.neoforged.neoforge.common.util.ClockAdjustment;
import org.bukkit.Bukkit;
import org.bukkit.event.world.TimeSkipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_NeoForge implements ServerLevelBridge {

    // @formatter:off
    @Shadow protected abstract void wakeUpAllPlayers();
    // @formatter:on

    private transient boolean arclight$timeSkipCancelled;

    // 26.1: night skip advances via ClockAdjustment (WAKE_UP_FROM_SLEEP), not setDayTime.
    @Decorate(method = "tick", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/util/ClockAdjustment;apply(Lnet/minecraft/world/clock/ServerClockManager;Lnet/minecraft/core/Holder;)V"))
    private void arclight$timeSkip(ClockAdjustment adjustment, ServerClockManager manager, Holder<WorldClock> clock) throws Throwable {
        long dayTime = manager.getTotalTicks(clock);
        long morning = dayTime + 24000L;
        long skipAmount = (morning - morning % 24000L) - dayTime;
        TimeSkipEvent event = new TimeSkipEvent(this.bridge$getWorld(), TimeSkipEvent.SkipReason.NIGHT_SKIP, skipAmount);
        Bukkit.getPluginManager().callEvent(event);
        arclight$timeSkipCancelled = event.isCancelled();
        if (event.isCancelled()) {
            return;
        }
        if (event.getSkipAmount() != skipAmount) {
            manager.setTotalTicks(clock, dayTime + event.getSkipAmount());
            return;
        }
        DecorationOps.callsite().invoke(adjustment, manager, clock);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V"))
    private void arclight$notWakeIfCancelled(ServerLevel world) {
        if (!arclight$timeSkipCancelled) {
            this.wakeUpAllPlayers();
        }
        arclight$timeSkipCancelled = false;
    }
}
