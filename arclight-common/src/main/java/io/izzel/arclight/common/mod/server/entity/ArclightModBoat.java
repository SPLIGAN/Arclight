package io.izzel.arclight.common.mod.server.entity;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftBoat;

/**
 * Fallback Craft wrapper for modded / unmapped {@link AbstractBoat} instances.
 * Vanilla wood types use per-type Craft*Boat classes via {@link org.bukkit.craftbukkit.entity.CraftEntityTypes}.
 */
public class ArclightModBoat extends CraftBoat {

    public ArclightModBoat(CraftServer server, AbstractBoat entity) {
        super(server, entity);
    }
}
