package com.github.standobyte.jojo.capability.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerUtilCapProvider implements ICapabilitySerializable<Tag> {
    @CapabilityInject(PlayerUtilCap.class)
    public static Capability<PlayerUtilCap> CAPABILITY = null;
    private LazyOptional<PlayerUtilCap> instance;
    
    public PlayerUtilCapProvider(Player player) {
        this.instance = LazyOptional.of(() -> new PlayerUtilCap(player));
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }
    
    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Player capability LazyOptional is not attached.")), null);
    }
    
    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Player capability LazyOptional is not attached.")), null, nbt);
    }

}