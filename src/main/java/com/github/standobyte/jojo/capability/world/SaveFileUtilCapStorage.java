package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundTag;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCap;

/**
 * 1.20.1 removed Capability.IStorage, so the read/write logic that used to
 * implement that interface lives here as plain helpers that the provider
 * calls. The class is kept so the original split between capability storage
 * and capability providers stays visible, and the serialized data is byte
 * identical to 1.16.5.
 */
public class SaveFileUtilCapStorage {

    public static CompoundTag writeNBT(SaveFileUtilCap instance) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("ServerData", instance.save());
        return nbt;
    }

    public static void readNBT(SaveFileUtilCap instance, CompoundTag nbt) {
        instance.load(nbt.contains("ServerData") ? nbt.getCompound("ServerData") : new CompoundTag());
    }
}
