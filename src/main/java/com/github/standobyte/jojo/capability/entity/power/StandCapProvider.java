package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class StandCapProvider implements ICapabilitySerializable<Tag> {
    @CapabilityInject(IStandPower.class)
    public static Capability<IStandPower> STAND_CAP = null;
    private LazyOptional<IStandPower> instance;
    
    public StandCapProvider(LivingEntity user) {
        this.instance = LazyOptional.of(() -> new StandPower(user));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return STAND_CAP.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return STAND_CAP.getStorage().writeNBT(STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Stand capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        STAND_CAP.getStorage().readNBT(STAND_CAP, instance.orElseThrow(
                () -> new IllegalArgumentException("Stand capability LazyOptional is not attached.")), null, nbt);
    }

}
