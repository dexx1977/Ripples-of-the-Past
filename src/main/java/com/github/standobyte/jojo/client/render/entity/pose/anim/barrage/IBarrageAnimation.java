package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

@Deprecated
public interface IBarrageAnimation<T extends Entity, M extends EntityModel<T>> extends IActionAnimation<T> {
    void addSwings(T entity, HumanoidArm side, float ticks);
    void animateSwing(T entity, M model, float loopCompletion, HumanoidArm side, float yRotOffsetRad, float xRotRad, float zRotOffsetRad);
    default void beforeSwingAfterimageRender(PoseStack matrixStack, M model, float loopCompletion, HumanoidArm side) {}
}
