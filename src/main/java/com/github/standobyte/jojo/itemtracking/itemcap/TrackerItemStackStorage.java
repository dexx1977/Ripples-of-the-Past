package com.github.standobyte.jojo.itemtracking.itemcap;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TrackerItemStackStorage implements IStorage<TrackerItemStack> {

    @Override
    public Tag writeNBT(Capability<TrackerItemStack> capability, TrackerItemStack instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<TrackerItemStack> capability, TrackerItemStack instance, Direction side, Tag nbt) {
        instance.fromNBT(nbt);
    }
}