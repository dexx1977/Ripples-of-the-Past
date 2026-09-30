package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WorldUtilCapStorage implements IStorage<WorldUtilCap> {

    @Override
    public Tag writeNBT(Capability<WorldUtilCap> capability, WorldUtilCap instance, Direction side) {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    @Override
    public void readNBT(Capability<WorldUtilCap> capability, WorldUtilCap instance, Direction side, Tag nbt) {
    }
}