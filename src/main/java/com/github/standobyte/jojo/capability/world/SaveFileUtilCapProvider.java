package com.github.standobyte.jojo.capability.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SaveFileUtilCapProvider implements ICapabilitySerializable<CompoundTag>{
    // the capability is attached to the overworld level (see ForgeBusEventSubscriber)
    public static SaveFileUtilCap getSaveFileCap(Level level) {
        return level.getCapability(CAPABILITY).orElseThrow(
                () -> new IllegalStateException("Save file capability is not attached to " + level.dimension().location()));
    }

    public static SaveFileUtilCap getSaveFileCap(MinecraftServer server) {
        return getSaveFileCap(server.overworld());
    }

    public static SaveFileUtilCap getSaveFileCap(ServerPlayer player) {
        return getSaveFileCap(player.getServer().overworld());
    }

    public static final Capability<SaveFileUtilCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<SaveFileUtilCap> instance;
    
    public SaveFileUtilCapProvider(ServerLevel overworld) {
        this.instance = LazyOptional.of(() -> new SaveFileUtilCap(overworld));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return SaveFileUtilCapStorage.writeNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached.")));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        SaveFileUtilCapStorage.readNBT(instance.orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached.")), nbt);
    }
}
