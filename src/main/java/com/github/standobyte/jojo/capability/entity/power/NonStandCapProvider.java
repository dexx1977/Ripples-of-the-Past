package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class NonStandCapProvider implements ICapabilitySerializable<Tag> {
    @CapabilityInject(INonStandPower.class)
    public static Capability<INonStandPower> NON_STAND_CAP = null;
    private LazyOptional<INonStandPower> instance;
    
    public NonStandCapProvider(LivingEntity user) {
        this.instance = LazyOptional.of(() -> new NonStandPower(user));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return NON_STAND_CAP.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return NON_STAND_CAP.getStorage().writeNBT(NON_STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Non-stand power capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        NON_STAND_CAP.getStorage().readNBT(NON_STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Non-stand power capability LazyOptional is not attached.")), null, nbt);
    }

}
