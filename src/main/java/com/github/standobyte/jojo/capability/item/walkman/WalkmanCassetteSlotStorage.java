package com.github.standobyte.jojo.capability.item.walkman;

import net.minecraft.nbt.Tag;

/**
 * 1.20.1 removed Capability.IStorage, so the walkman slot's read/write logic
 * lives here as plain helpers that the provider calls. Forge's ItemStackHandler
 * accepts both the legacy compound form and the current list form, so walkman
 * items written by either layout keep working.
 */
public class WalkmanCassetteSlotStorage {

    public static Tag writeNBT(WalkmanCassetteSlotCap instance) {
        return instance.serializeNBT();
    }

    public static void readNBT(WalkmanCassetteSlotCap instance, Tag nbt) {
        instance.deserializeNBT(nbt);
    }
}
