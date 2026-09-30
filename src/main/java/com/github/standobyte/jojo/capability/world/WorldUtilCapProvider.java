package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WorldUtilCapProvider implements ICapabilitySerializable<Tag>{
    @CapabilityInject(WorldUtilCap.class)
    public static Capability<WorldUtilCap> CAPABILITY = null;
    private LazyOptional<WorldUtilCap> instance;
    
    public WorldUtilCapProvider(Level world) {
        this.instance = LazyOptional.of(() -> new WorldUtilCap(world));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("World capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("World capability LazyOptional is not attached.")), null, nbt);
    }
}
