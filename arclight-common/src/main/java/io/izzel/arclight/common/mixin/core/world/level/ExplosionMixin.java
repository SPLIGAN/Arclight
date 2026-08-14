package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.world.damagesource.DamageSourceBridge;
import io.izzel.arclight.common.bridge.core.world.level.ExplosionBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC 26.1+: {@link Explosion} is an interface; server logic lives in {@link ServerExplosion}.
 * Full Bukkit explode event port is soft-debt; bridge surface kept for callers.
 */
@Mixin(ServerExplosion.class)
public abstract class ExplosionMixin implements ExplosionBridge {

	@Shadow @Final private ServerLevel level;
	@Shadow @Final private Explosion.BlockInteraction blockInteraction;
	@Shadow @Final @Mutable private float radius;
	@Shadow @Final private Vec3 center;
	@Shadow @Final private Entity source;
	@Shadow @Final @Mutable private DamageSource damageSource;

	@Shadow
	public abstract Explosion.BlockInteraction getBlockInteraction();

	public float yield;
	public boolean wasCanceled = false;

	@Inject(
		method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
		at = @At("RETURN")
	)
	private void arclight$init(
		ServerLevel level,
		Entity entity,
		DamageSource damageSource,
		ExplosionDamageCalculator calculator,
		Vec3 center,
		float radius,
		boolean fire,
		Explosion.BlockInteraction blockInteraction,
		CallbackInfo ci
	) {
		this.radius = Math.max(radius, 0.0F);
		this.yield = this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY
			? 1.0F / Math.max(this.radius, 0.1F)
			: 1.0F;
		if (this.damageSource != null) {
			this.damageSource = ((DamageSourceBridge) this.damageSource).bridge$customCausingEntity(entity);
		}
	}

	@Inject(method = "explode", cancellable = true, at = @At("HEAD"))
	private void arclight$returnRadius(CallbackInfoReturnable<Integer> cir) {
		if (this.radius < 0.1F) {
			cir.setReturnValue(0);
		}
	}

	@Override
	public Entity bridge$getExploder() {
		return this.source;
	}

	@Override
	public float bridge$getSize() {
		return this.radius;
	}

	@Override
	public void bridge$setSize(float size) {
		this.radius = size;
	}

	@Override
	public Explosion.BlockInteraction bridge$getMode() {
		return this.blockInteraction;
	}

	@Override
	public boolean bridge$wasCancelled() {
		return this.wasCanceled;
	}

	@Override
	public float bridge$getYield() {
		return this.yield;
	}
}
