package com.github.standobyte.jojo.capability.entity.hamonutil;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ProjectileHamonChargeCapProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<ProjectileHamonChargeCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<ProjectileHamonChargeCap> instance;
    
    public ProjectileHamonChargeCapProvider(Entity projectile) {
        this.instance = LazyOptional.of(() -> new ProjectileHamonChargeCap(projectile));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return ProjectileHamonChargeCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Projectile capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ProjectileHamonChargeCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Projectile capability LazyOptional is not attached.")), nbt);
    }
}
