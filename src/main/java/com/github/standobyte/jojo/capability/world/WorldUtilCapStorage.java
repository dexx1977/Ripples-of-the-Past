package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundTag;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;

/**
 * 1.20.1 removed Capability.IStorage, so the read/write logic that used to
 * implement that interface lives here as plain helpers that the provider
 * calls. The class is kept so the original split between capability storage
 * and capability providers stays visible, and the serialized data is byte
 * identical to 1.16.5.
 */
public class WorldUtilCapStorage {

    public static CompoundTag writeNBT(WorldUtilCap instance) {
        return new CompoundTag();
    }

    public static void readNBT(WorldUtilCap instance, CompoundTag nbt) {
        // nothing persisted for this capability, as in 1.16.5
    }
}
