package com.github.standobyte.jojo.capability.world;

import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MrPresidentWorldDataProvider implements ICapabilitySerializable<Tag>{
    @CapabilityInject(MrPresidentWorldData.class)
    public static Capability<MrPresidentWorldData> CAPABILITY = null;
    private LazyOptional<MrPresidentWorldData> instance;
    
    public MrPresidentWorldDataProvider(ServerLevel world) {
        this.instance = LazyOptional.of(() -> new MrPresidentWorldData(world));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Mr.President capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Mr.President capability LazyOptional is not attached.")), null, nbt);
    }
}
