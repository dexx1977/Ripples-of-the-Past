package com.github.standobyte.jojo.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PlayerUtilCapStorage implements IStorage<PlayerUtilCap> {

    @Override
    public Tag writeNBT(Capability<PlayerUtilCap> capability, PlayerUtilCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<PlayerUtilCap> capability, PlayerUtilCap instance, Direction side, Tag nbt) {
        instance.fromNBT((CompoundTag) nbt);
    }
}