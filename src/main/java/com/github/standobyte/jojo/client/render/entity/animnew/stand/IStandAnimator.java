package com.github.standobyte.jojo.client.render.entity.animnew.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IStandAnimator {
    boolean isLegacy();
    <T extends StandEntity> boolean poseStand(@Nullable T entity, StandEntityModel<T> model, StandPoseData pose, 
            float ticks, float yRotOffsetDeg, float xRotDeg);
    <T extends StandEntity> void poseStandPost(@Nullable T standEntity, StandEntityModel<T> standEntityModel);
    
    <T extends StandEntity> void addBarrageSwings(T entity, StandEntityModel<T> model, float ticks);
    <T extends StandEntity> void renderBarrageSwings(T entity, StandEntityModel<T> model, float yRotOffsetDeg, float xRotDeg, 
            PoseStack matrixStack, VertexConsumer buffer, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
}
