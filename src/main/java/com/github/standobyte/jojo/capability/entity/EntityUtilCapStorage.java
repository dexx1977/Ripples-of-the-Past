package com.github.standobyte.jojo.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EntityUtilCapStorage implements IStorage<EntityUtilCap> {

    @Override
    public Tag writeNBT(Capability<EntityUtilCap> capability, EntityUtilCap instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<EntityUtilCap> capability, EntityUtilCap instance, Direction side, Tag nbt) {
        instance.deserializeNBT((CompoundTag) nbt);
    }
}