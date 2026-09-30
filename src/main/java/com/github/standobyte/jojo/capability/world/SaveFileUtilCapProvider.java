package com.github.standobyte.jojo.capability.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SaveFileUtilCapProvider implements ICapabilitySerializable<Tag>{
    @CapabilityInject(SaveFileUtilCap.class)
    public static Capability<SaveFileUtilCap> CAPABILITY = null;
    private LazyOptional<SaveFileUtilCap> instance;
    
    public SaveFileUtilCapProvider(ServerLevel overworld) {
        this.instance = LazyOptional.of(() -> new SaveFileUtilCap(overworld));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached.")), null, nbt);
    }
    
    public static SaveFileUtilCap getSaveFileCap(MinecraftServer server) {
        return server.overworld().getCapability(SaveFileUtilCapProvider.CAPABILITY).orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached."));
    }
    
    public static SaveFileUtilCap getSaveFileCap(ServerLevel serverWorld) {
        return getSaveFileCap(serverWorld.getServer());
    }
    
    public static SaveFileUtilCap getSaveFileCap(ServerPlayer serverPlayer) {
        return getSaveFileCap(serverPlayer.server);
    }
}
