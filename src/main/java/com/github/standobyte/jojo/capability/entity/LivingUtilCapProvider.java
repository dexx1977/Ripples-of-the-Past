package com.github.standobyte.jojo.capability.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LivingUtilCapProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<LivingUtilCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<LivingUtilCap> instance;
    
    public LivingUtilCapProvider(LivingEntity entity) {
        this.instance = LazyOptional.of(() -> new LivingUtilCap(entity));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return LivingUtilCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Living capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        LivingUtilCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Living capability LazyOptional is not attached.")), nbt);
    }
}
