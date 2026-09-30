package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EntityHamonChargeCapProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<EntityHamonChargeCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<EntityHamonChargeCap> instance;
    
    public EntityHamonChargeCapProvider(Entity entity) {
        this.instance = LazyOptional.of(() -> new EntityHamonChargeCap(entity));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return EntityHamonChargeCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Hamon charge capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        EntityHamonChargeCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Hamon charge capability LazyOptional is not attached.")), nbt);
    }
}
