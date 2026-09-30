package com.github.standobyte.jojo.capability.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EntityUtilCapProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<EntityUtilCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<EntityUtilCap> instance;
    
    public EntityUtilCapProvider(Entity entity) {
        this.instance = LazyOptional.of(() -> new EntityUtilCap(entity));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return EntityUtilCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Entity capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        EntityUtilCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Entity capability LazyOptional is not attached.")), nbt);
    }
}
