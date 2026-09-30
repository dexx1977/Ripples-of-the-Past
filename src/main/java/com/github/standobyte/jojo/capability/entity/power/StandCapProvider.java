package com.github.standobyte.jojo.capability.entity.power;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.power.StandCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class StandCapProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IStandPower> STAND_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<IStandPower> instance;
    
    public StandCapProvider(LivingEntity user) {
        this.instance = LazyOptional.of(() -> new StandPower(user));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return STAND_CAP.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return StandCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Stand capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        StandCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Stand capability LazyOptional is not attached.")), nbt);
    }
}
