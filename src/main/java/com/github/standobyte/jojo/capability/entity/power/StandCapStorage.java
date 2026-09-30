package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StandCapStorage implements IStorage<IStandPower> {

    @Override
    public Tag writeNBT(Capability<IStandPower> capability, IStandPower instance, Direction side) {
        return instance.writeNBT();
    }

    @Override
    public void readNBT(Capability<IStandPower> capability, IStandPower instance, Direction side, Tag nbt) {
        instance.readNBT((CompoundTag) nbt);
    }
}
