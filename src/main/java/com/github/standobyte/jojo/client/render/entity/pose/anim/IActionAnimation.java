package com.github.standobyte.jojo.client.render.entity.pose.anim;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

public interface IActionAnimation<T extends Entity> {
    
    @Deprecated
    default void onAnimStart(T entity, float yRotationOffset, float xRotation) {}
    
    void animate(StandEntityAction.Phase phase, float phaseCompletion, 
            T entity, float ticks, float yRotOffsetRad, float xRotRad, HumanoidArm side);
    
    default void renderAdditional(T entity, PoseStack matrixStack, VertexConsumer buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {}
}
