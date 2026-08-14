package io.izzel.arclight.common.mod.server.entity;

import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftChestBoat;

/**
 * Fallback Craft wrapper for modded / unmapped {@link AbstractChestBoat} instances.
 */
public class ArclightModChestBoat extends CraftChestBoat {

    public ArclightModChestBoat(CraftServer server, AbstractChestBoat entity) {
        super(server, entity);
    }
}
