package com.github.standobyte.jojo.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class LivingUtilCapStorage implements IStorage<LivingUtilCap> {

    @Override
    public Tag writeNBT(Capability<LivingUtilCap> capability, LivingUtilCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<LivingUtilCap> capability, LivingUtilCap instance, Direction side, Tag nbt) {
        instance.fromNBT((CompoundTag) nbt);
    }
}