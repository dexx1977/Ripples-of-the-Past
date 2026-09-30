package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

public class ModelPoseSided<T extends Entity> implements IModelPose<T> {
    private final Map<HumanoidArm, IModelPose<T>> poses = new EnumMap<>(HumanoidArm.class);
    
    public ModelPoseSided(IModelPose<T> poseLeft, IModelPose<T> poseRight) {
        poses.put(HumanoidArm.LEFT, poseLeft);
        poses.put(HumanoidArm.RIGHT, poseRight);
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad, HumanoidArm side) {
        poses.get(side).poseModel(rotationAmount, entity, ticks, yRotOffsetRad, xRotRad, side);
    }
    
    @Override
    public ModelPoseSided<T> setEasing(UnaryOperator<Float> function) {
        for (IModelPose<T> pose : poses.values()) {
            pose.setEasing(function);
        }
        return this;
    }
}
