package com.github.standobyte.jojo.capability.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import com.github.standobyte.jojo.capability.world.WorldUtilCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WorldUtilCapProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<WorldUtilCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<WorldUtilCap> instance;
    
    public WorldUtilCapProvider(Level world) {
        this.instance = LazyOptional.of(() -> new WorldUtilCap(world));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return WorldUtilCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("World capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        WorldUtilCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("World capability LazyOptional is not attached.")), nbt);
    }
}
