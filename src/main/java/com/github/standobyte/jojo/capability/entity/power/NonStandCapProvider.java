package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.power.NonStandCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class NonStandCapProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<INonStandPower> NON_STAND_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<INonStandPower> instance;
    
    public NonStandCapProvider(LivingEntity user) {
        this.instance = LazyOptional.of(() -> new NonStandPower(user));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return NON_STAND_CAP.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return NonStandCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Non-stand power capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        NonStandCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Non-stand power capability LazyOptional is not attached.")), nbt);
    }
}
