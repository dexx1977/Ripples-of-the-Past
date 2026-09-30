package com.github.standobyte.jojo.capability.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

public interface PlayerMixinExtension {
    void toNBT(CompoundTag forgeCapNbt);
    void fromNBT(CompoundTag forgeCapNbt);
    void syncToClient(ServerPlayer thisAsPlayer);
    void syncToTracking(ServerPlayer tracking);
}
