package com.github.standobyte.jojo.capability.entity;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class ClientPlayerUtilCapProvider implements ICapabilityProvider {
    public static final Capability<ClientPlayerUtilCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<ClientPlayerUtilCap> instance;
    
    public ClientPlayerUtilCapProvider(Player player) {
        this.instance = LazyOptional.of(() -> new ClientPlayerUtilCap((AbstractClientPlayer) player));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

}