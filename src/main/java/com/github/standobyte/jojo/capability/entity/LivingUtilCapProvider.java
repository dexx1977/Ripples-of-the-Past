package com.github.standobyte.jojo.capability.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LivingUtilCapProvider implements ICapabilitySerializable<Tag>{
    @CapabilityInject(LivingUtilCap.class)
    public static Capability<LivingUtilCap> CAPABILITY = null;
    private LazyOptional<LivingUtilCap> instance;
    
    public LivingUtilCapProvider(LivingEntity entity) {
        this.instance = LazyOptional.of(() -> new LivingUtilCap(entity));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Living capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Living capability LazyOptional is not attached.")), null, nbt);
    }
}
