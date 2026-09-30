package com.github.standobyte.jojo.capability.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerUtilCapProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<PlayerUtilCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<PlayerUtilCap> instance;
    
    public PlayerUtilCapProvider(Player player) {
        this.instance = LazyOptional.of(() -> new PlayerUtilCap(player));
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }
    
    @Override
    public CompoundTag serializeNBT() {
        return PlayerUtilCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Player capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        PlayerUtilCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Player capability LazyOptional is not attached.")), nbt);
    }
}
