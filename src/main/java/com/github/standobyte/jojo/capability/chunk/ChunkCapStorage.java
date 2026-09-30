package com.github.standobyte.jojo.capability.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ChunkCapStorage implements IStorage<ChunkCap> {

    @Override
    public Tag writeNBT(Capability<ChunkCap> capability, ChunkCap instance, Direction side) {
        return instance.save();
    }

    @Override
    public void readNBT(Capability<ChunkCap> capability, ChunkCap instance, Direction side, Tag nbt) {
        instance.load((CompoundTag) nbt);
    }
}
