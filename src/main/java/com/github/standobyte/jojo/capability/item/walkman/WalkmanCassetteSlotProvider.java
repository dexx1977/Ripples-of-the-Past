package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class WalkmanCassetteSlotProvider implements ICapabilitySerializable<Tag> {
    private LazyOptional<IItemHandler> instance;
    
    public WalkmanCassetteSlotProvider(ItemStack itemStack) {
        this.instance = LazyOptional.of(() -> new WalkmanCassetteSlotCap(itemStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (!(nbt instanceof ListTag)) return;
        
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Walkman item capability LazyOptional is not attached.")), null, nbt);
    }

}
