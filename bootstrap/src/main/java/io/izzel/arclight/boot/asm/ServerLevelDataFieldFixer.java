package io.izzel.arclight.boot.asm;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * CraftBukkit types {@code ServerLevel.serverLevelData} as {@link net.minecraft.world.level.storage.PrimaryLevelData}.
 * Vanilla keeps {@code ServerLevelData}; Arclight stores the Craft view in soft field {@code K}.
 * Rewrite CraftBukkit field ops that use the PrimaryLevelData descriptor onto {@code K}.
 */
public class ServerLevelDataFieldFixer implements Implementer {

    private static final Marker MARKER = MarkerManager.getMarker("LEVEL_DATA");
    private static final String OWNER = "net/minecraft/server/level/ServerLevel";
    private static final String VANILLA_NAME = "serverLevelData";
    private static final String CRAFT_DESC = "Lnet/minecraft/world/level/storage/PrimaryLevelData;";
    private static final String ARCLIGHT_NAME = "K";

    @Override
    public boolean processClass(ClassNode node) {
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (method.instructions == null) {
                continue;
            }
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FieldInsnNode fi
                    && (fi.getOpcode() == Opcodes.GETFIELD || fi.getOpcode() == Opcodes.PUTFIELD)
                    && OWNER.equals(fi.owner)
                    && VANILLA_NAME.equals(fi.name)
                    && CRAFT_DESC.equals(fi.desc)) {
                    fi.name = ARCLIGHT_NAME;
                    changed = true;
                }
            }
        }
        if (changed) {
            Implementer.LOGGER.debug(MARKER, "Rewrote CraftBukkit PrimaryLevelData serverLevelData accesses in {}", node.name);
        }
        return changed;
    }
}
