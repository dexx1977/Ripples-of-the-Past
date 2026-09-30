package com.github.standobyte.jojo.itemtracking.itemcap;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStackStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TrackerItemStackProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<TrackerItemStack> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<TrackerItemStack> instance;
    
    public TrackerItemStackProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new TrackerItemStack(itemStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return TrackerItemStackStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        TrackerItemStackStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Capability LazyOptional is not attached.")), nbt);
    }
}
