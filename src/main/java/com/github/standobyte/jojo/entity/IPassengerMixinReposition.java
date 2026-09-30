package com.github.standobyte.jojo.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IPassengerMixinReposition {

    @Nullable Vec3 repositionPassenger(@Nonnull Entity vehicle);
}
