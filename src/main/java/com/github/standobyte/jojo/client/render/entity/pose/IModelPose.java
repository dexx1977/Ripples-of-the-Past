package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.function.UnaryOperator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

public interface IModelPose<T extends Entity> {
    void poseModel(float rotationAmount, T entity, float ticks, 
            float yRotOffsetRad, float xRotRad, HumanoidArm side);
    IModelPose<T> setEasing(UnaryOperator<Float> function);
}
