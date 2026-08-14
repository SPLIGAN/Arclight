package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftChunk.class)
public class CraftChunkMixin {

	// CraftBukkit compiles LevelChunk.level as ServerLevel; NeoForge keeps Level.
	@Redirect(
		method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/level/chunk/LevelChunk;level:Lnet/minecraft/server/level/ServerLevel;",
			opcode = Opcodes.GETFIELD,
			remap = false
		)
	)
	private static ServerLevel arclight$levelAsServerLevel(LevelChunk chunk) {
		return (ServerLevel) chunk.level;
	}
}
