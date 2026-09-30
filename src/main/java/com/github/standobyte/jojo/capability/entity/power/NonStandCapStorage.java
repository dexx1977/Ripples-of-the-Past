package com.github.standobyte.jojo.capability.entity.power;

import net.minecraft.nbt.CompoundTag;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

/**
 * 1.20.1 removed Capability.IStorage, so the read/write logic that used to
 * implement that interface lives here as plain helpers that the provider
 * calls. The class is kept so the original split between capability storage
 * and capability providers stays visible, and the serialized data is byte
 * identical to 1.16.5.
 */
public class NonStandCapStorage {

    public static CompoundTag writeNBT(INonStandPower instance) {
        return (CompoundTag) instance.writeNBT(); // the power interface declares Tag, the data is a compound
    }

    public static void readNBT(INonStandPower instance, CompoundTag nbt) {
        instance.readNBT(nbt);
    }
}
