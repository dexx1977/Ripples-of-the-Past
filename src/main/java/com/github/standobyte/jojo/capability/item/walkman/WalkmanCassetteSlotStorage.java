package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class WalkmanCassetteSlotStorage implements IStorage<WalkmanCassetteSlotCap> {

    @Override
    public Tag writeNBT(Capability<WalkmanCassetteSlotCap> capability, WalkmanCassetteSlotCap instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<WalkmanCassetteSlotCap> capability, WalkmanCassetteSlotCap instance, Direction side, Tag nbt) {
        instance.deserializeNBT((CompoundTag) nbt);
    }
}