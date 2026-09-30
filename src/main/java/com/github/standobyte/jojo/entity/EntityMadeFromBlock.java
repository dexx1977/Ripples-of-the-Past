package com.github.standobyte.jojo.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

public interface EntityMadeFromBlock {
    boolean crazyDRestore(BlockPos blockPos);
    default boolean isEntityAlive() {
        return ((Entity) this).isAlive();
    }
}
