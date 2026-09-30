package com.github.standobyte.jojo.itemtracking.itemcap;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TrackerItemStackProvider implements ICapabilitySerializable<Tag>{
    @CapabilityInject(TrackerItemStack.class)
    public static Capability<TrackerItemStack> CAPABILITY = null;
    private LazyOptional<TrackerItemStack> instance;
    
    public TrackerItemStackProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new TrackerItemStack(itemStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Capability LazyOptional is not attached.")), null, nbt);
    }
}
