package io.izzel.arclight.common.mod.server.block;

import io.izzel.arclight.api.Unsafe;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;

import java.lang.reflect.Field;

/**
 * Vanilla/NeoForge 26.1 double-chest {@link net.minecraft.world.MenuProvider} is the anonymous
 * {@code ChestBlock$2$1} holding a {@link CompoundContainer} in {@code val$container}.
 * Spigot's old {@code DoubleChestCombiner.Dummy.DoubleInventory} / {@code inventorylargechest} do not exist here.
 */
public class ChestBlockDoubleInventoryHacks {

    private static final Class<?> cl;
    private static final long offset;

    static {
        try {
            cl = Class.forName("net.minecraft.world.level.block.ChestBlock$2$1");
            Field field = cl.getDeclaredField("val$container");
            offset = Unsafe.objectFieldOffset(field);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CompoundContainer get(Object obj) {
        Container container = (Container) Unsafe.getObject(obj, offset);
        if (container instanceof CompoundContainer compoundContainer) {
            return compoundContainer;
        }
        throw new IllegalStateException("Expected CompoundContainer in ChestBlock$2$1.val$container, got " + (container == null ? "null" : container.getClass()));
    }

    public static boolean isInstance(Object obj) {
        return cl.isInstance(obj);
    }
}
