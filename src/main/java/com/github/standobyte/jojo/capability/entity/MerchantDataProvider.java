package com.github.standobyte.jojo.capability.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MerchantDataProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<MerchantData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<MerchantData> instance;
    
    public MerchantDataProvider(LivingEntity entity, Merchant asMerchant) {
        this.instance = LazyOptional.of(() -> new MerchantData(entity, asMerchant));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElseThrow(
                () -> new IllegalArgumentException("Merchant data LazyOptional is not attached.")).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.orElseThrow(
                () -> new IllegalArgumentException("Merchant data LazyOptional is not attached.")).deserializeNBT(nbt);
    }
}
