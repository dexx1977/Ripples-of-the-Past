package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WalkmanCassetteSlotProvider implements ICapabilitySerializable<Tag> {
    private LazyOptional<WalkmanCassetteSlotCap> instance;

    public WalkmanCassetteSlotProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new WalkmanCassetteSlotCap(itemStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        // CapabilityItemHandler was replaced by ForgeCapabilities in 1.19+.
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return WalkmanCassetteSlotStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt == null) return;

        WalkmanCassetteSlotStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), nbt);
    }

}
