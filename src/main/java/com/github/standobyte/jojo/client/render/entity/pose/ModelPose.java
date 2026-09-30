package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

public class ModelPose<T extends Entity> implements IModelPose<T> {
    public final Map<ModelPart, RotationAngle> rotations = new HashMap<>();
    private UnaryOperator<Float> easingFunc = x -> x;
    private ModelAnim<T> additionalAnim = null;

    public ModelPose(RotationAngle... rotations) {
        for (RotationAngle rotation : rotations) {
            putRotation(rotation);
        }
    }
    
    public ModelPose<T> putRotation(RotationAngle rotation) {
        if (rotation.modelRenderer != null) {
            rotations.put(rotation.modelRenderer, rotation);
        }
        return this;
    }

    @Override
    public ModelPose<T> setEasing(UnaryOperator<Float> function) {
        this.easingFunc = function;
        return this;
    }

    public ModelPose<T> setAdditionalAnim(ModelAnim<T> anim) {
        this.additionalAnim = anim;
        return this;
    }
    
    public RigidModelPose<T> createRigid() {
        return new RigidModelPose<T>(this);
    }
    
    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad, HumanoidArm side) {
        rotationAmount = easingFunc.apply(rotationAmount);
        applyRotations(rotationAmount);
        additionalAnim(rotationAmount, entity, ticks, yRotOffsetRad, xRotRad);
    }
    
    protected void applyRotations(float rotationAmount) {
        for (Map.Entry<ModelPart, RotationAngle> rotation : rotations.entrySet()) {
            rotation.getValue().applyRotation(rotationAmount);
        }
    }
    
    protected void additionalAnim(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad) {
        if (additionalAnim != null) {
            additionalAnim.setupAnim(rotationAmount, entity, ticks, yRotOffsetRad, xRotRad);
        }
    }
    
    public ModelPose<T> copy() {
        ModelPose<T> pose = new ModelPose<T>();
        this.rotations.forEach((key, value) -> pose.rotations.put(key, value));
        pose.easingFunc = this.easingFunc;
        pose.additionalAnim = this.additionalAnim;
        return pose;
    }
    
    
    
    public static interface ModelAnim<T extends Entity> {
        void setupAnim(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad);
    }
}
