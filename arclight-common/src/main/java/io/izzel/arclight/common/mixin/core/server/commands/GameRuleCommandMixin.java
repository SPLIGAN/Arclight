package io.izzel.arclight.common.mixin.core.server.commands;

import net.minecraft.server.commands.GameRuleCommand;
import org.spongepowered.asm.mixin.Mixin;

// 26.1 GameRuleCommand already uses CommandSourceStack.getLevel().getGameRules()
@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
}
