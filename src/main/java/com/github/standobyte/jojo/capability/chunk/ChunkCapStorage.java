package com.github.standobyte.jojo.capability.chunk;

import net.minecraft.nbt.CompoundTag;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;

/**
 * 1.20.1 removed Capability.IStorage, so the read/write logic that used to
 * implement that interface lives here as plain helpers that the provider
 * calls. The class is kept so the original split between capability storage
 * and capability providers stays visible, and the serialized data is byte
 * identical to 1.16.5.
 */
public class ChunkCapStorage {

    public static CompoundTag writeNBT(ChunkCap instance) {
        return instance.save();
    }

    public static void readNBT(ChunkCap instance, CompoundTag nbt) {
        instance.load(nbt);
    }
}
