package io.izzel.arclight.neoforge.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.world.level.ExplosionBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class ExplosionMixin_NeoForge implements ExplosionBridge {

	@Override
	public void bridge$forge$onExplosionDetonate(Level level, Explosion explosion, List<Entity> list, double diameter) {
		if (level instanceof ServerLevel && explosion instanceof ServerExplosion serverExplosion) {
			EventHooks.onExplosionDetonate(level, serverExplosion, list, Collections.emptyList());
		}
	}
}
