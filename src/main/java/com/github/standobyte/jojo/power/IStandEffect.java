package com.github.standobyte.jojo.power;

import net.minecraft.world.entity.LivingEntity;

public interface IStandEffect {
    void onStarted(LivingEntity entity, LivingEntity user);
    void tick(LivingEntity entity, LivingEntity user);
    void onStopped(LivingEntity entity, LivingEntity user);
}
