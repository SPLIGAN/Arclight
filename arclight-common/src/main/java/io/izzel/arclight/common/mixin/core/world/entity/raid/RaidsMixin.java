package io.izzel.arclight.common.mixin.core.world.entity.raid;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Raids.class)
public abstract class RaidsMixin {

    // @formatter:off
    @Shadow @Final public Int2ObjectMap<Raid> raidMap;
    @Shadow public abstract java.util.OptionalInt getId(Raid raid);
    // @formatter:on

    @Decorate(method = "createOrExtendRaid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;absorbRaidOmen(Lnet/minecraft/server/level/ServerPlayer;)Z"))
    private boolean arclight$raidTrigger(Raid raid, ServerPlayer player) throws Throwable {
        if (!CraftEventFactory.callRaidTriggerEvent(raid, (ServerLevel) player.level(), player)) {
            player.removeEffect(MobEffects.RAID_OMEN);
            player.removeEffect(MobEffects.BAD_OMEN);
            this.getId(raid).ifPresent(id -> this.raidMap.remove(id, raid));
            return (boolean) DecorationOps.cancel().invoke((Raid) null);
        }
        return (boolean) DecorationOps.callsite().invoke(raid, player);
    }
}
