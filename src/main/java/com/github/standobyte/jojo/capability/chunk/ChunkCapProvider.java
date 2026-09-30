package com.github.standobyte.jojo.capability.chunk;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ChunkCapProvider implements ICapabilitySerializable<Tag>{
    @CapabilityInject(ChunkCap.class)
    public static Capability<ChunkCap> CAPABILITY = null;
    private LazyOptional<ChunkCap> instance;
    
    public ChunkCapProvider(LevelChunk chunk) {
        this.instance = LazyOptional.of(() -> new ChunkCap(chunk));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Chunk capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Chunk capability LazyOptional is not attached.")), null, nbt);
    }

}
