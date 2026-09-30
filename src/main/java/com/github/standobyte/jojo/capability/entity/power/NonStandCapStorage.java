package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class NonStandCapStorage implements IStorage<INonStandPower> {

    @Override
    public Tag writeNBT(Capability<INonStandPower> capability, INonStandPower instance, Direction side) {
        return instance.writeNBT();
    }

    @Override
    public void readNBT(Capability<INonStandPower> capability, INonStandPower instance, Direction side, Tag nbt) {
        instance.readNBT((CompoundTag) nbt);
    }
}
