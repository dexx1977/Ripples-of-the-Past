package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EntityHamonChargeCapStorage implements IStorage<EntityHamonChargeCap> {

    @Override
    public Tag writeNBT(Capability<EntityHamonChargeCap> capability, EntityHamonChargeCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<EntityHamonChargeCap> capability, EntityHamonChargeCap instance, Direction side, Tag nbt) {
        instance.fromNBT((CompoundTag) nbt);
    }
}