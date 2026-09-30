package com.github.standobyte.jojo.capability.world;

import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MrPresidentWorldDataProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<MrPresidentWorldData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<MrPresidentWorldData> instance;
    
    public MrPresidentWorldDataProvider(ServerLevel world) {
        this.instance = LazyOptional.of(() -> new MrPresidentWorldData(world));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElseThrow(
                () -> new IllegalArgumentException("Mr.President capability LazyOptional is not attached.")).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.orElseThrow(
                () -> new IllegalArgumentException("Mr.President capability LazyOptional is not attached.")).deserializeNBT(nbt);
    }
}
