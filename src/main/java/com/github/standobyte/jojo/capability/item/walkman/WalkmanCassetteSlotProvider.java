package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class WalkmanCassetteSlotProvider implements ICapabilitySerializable<CompoundTag> {
    private final WalkmanCassetteSlotCap cassetteSlot;
    // 1.16.5 exposed the handler through LazyOptional<IItemHandler>, as does 1.20.1.
    private final LazyOptional<IItemHandler> instance;

    public WalkmanCassetteSlotProvider(ItemStack itemStack) {
        this.cassetteSlot = new WalkmanCassetteSlotCap(itemStack);
        this.instance = LazyOptional.of(() -> this.cassetteSlot);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        // CapabilityItemHandler was replaced by ForgeCapabilities in 1.19+.
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return WalkmanCassetteSlotStorage.writeNBT(cassetteSlot);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;

        WalkmanCassetteSlotStorage.readNBT(cassetteSlot, nbt);
    }

}
