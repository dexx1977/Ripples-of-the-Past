package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.nbt.CompoundTag;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;

/**
 * 1.20.1 removed Capability.IStorage, so the read/write logic that used to
 * implement that interface lives here as plain helpers that the provider
 * calls. The class is kept so the original split between capability storage
 * and capability providers stays visible, and the serialized data is byte
 * identical to 1.16.5.
 */
public class ProjectileHamonChargeCapStorage {

    public static CompoundTag writeNBT(ProjectileHamonChargeCap instance) {
        return instance.toNBT();
    }

    public static void readNBT(ProjectileHamonChargeCap instance, CompoundTag nbt) {
        instance.fromNBT(nbt);
    }
}
