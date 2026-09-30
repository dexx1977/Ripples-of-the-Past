package com.github.standobyte.jojo.potion;

import net.minecraft.world.entity.LivingEntity;

public interface IApplicableEffect {
    boolean isApplicable(LivingEntity entity);
}
