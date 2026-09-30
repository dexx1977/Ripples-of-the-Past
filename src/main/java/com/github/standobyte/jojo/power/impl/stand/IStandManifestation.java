package com.github.standobyte.jojo.power.impl.stand;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

public interface IStandManifestation {
    void setUserAndPower(LivingEntity user, IStandPower power);
    void syncWithTrackingOrUser(ServerPlayer player);
}
