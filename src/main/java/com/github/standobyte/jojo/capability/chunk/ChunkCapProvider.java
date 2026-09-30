package com.github.standobyte.jojo.capability.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;
import com.github.standobyte.jojo.capability.chunk.ChunkCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ChunkCapProvider implements ICapabilitySerializable<CompoundTag>{
    public static final Capability<ChunkCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<ChunkCap> instance;
    
    public ChunkCapProvider(LevelChunk chunk) {
        this.instance = LazyOptional.of(() -> new ChunkCap(chunk));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return ChunkCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Chunk capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ChunkCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Chunk capability LazyOptional is not attached.")), nbt);
    }
}
