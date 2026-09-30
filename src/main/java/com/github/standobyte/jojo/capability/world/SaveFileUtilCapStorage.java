package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SaveFileUtilCapStorage implements IStorage<SaveFileUtilCap> {

    @Override
    public Tag writeNBT(Capability<SaveFileUtilCap> capability, SaveFileUtilCap instance, Direction side) {
        CompoundTag cnbt = new CompoundTag();
        cnbt.put("ServerData", instance.save());
        return cnbt;
    }

    @Override
    public void readNBT(Capability<SaveFileUtilCap> capability, SaveFileUtilCap instance, Direction side, Tag nbt) {
        CompoundTag cnbt = (CompoundTag) nbt;
        instance.load(cnbt.getCompound("ServerData"));
    }
}