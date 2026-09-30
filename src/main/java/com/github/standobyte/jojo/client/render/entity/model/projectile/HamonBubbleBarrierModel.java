package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleBarrierEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

// Made with Blockbench 3.9.2


public class HamonBubbleBarrierModel extends EntityModel<HamonBubbleBarrierEntity> {
    private final ModelPart bubble;

    public HamonBubbleBarrierModel() {
        super(RenderType::entityTranslucent);
        texWidth = 256;
        texHeight = 256;

        bubble = new ModelPart(this);
        bubble.setPos(0.0F, -15.0F, 0.0F);
        bubble.texOffs(0, 0).addBox(-12.0F, -12.0F, -12.0F, 24.0F, 24.0F, 24.0F, 0.0F, false);
        bubble.texOffs(96, 0).addBox(-9.0F, -9.0F, -15.0F, 18.0F, 18.0F, 30.0F, 0.0F, false);
        bubble.texOffs(0, 48).addBox(-6.0F, -6.0F, -16.5F, 12.0F, 12.0F, 33.0F, 0.0F, false);
        bubble.texOffs(90, 48).addBox(-15.0F, -9.0F, -9.0F, 30.0F, 18.0F, 18.0F, 0.0F, false);
        bubble.texOffs(90, 84).addBox(-16.5F, -6.0F, -6.0F, 33.0F, 12.0F, 12.0F, 0.0F, false);
        bubble.texOffs(0, 93).addBox(-9.0F, -15.0F, -9.0F, 18.0F, 30.0F, 18.0F, 0.0F, false);
        bubble.texOffs(72, 108).addBox(-6.0F, -16.5F, -6.0F, 12.0F, 33.0F, 12.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(HamonBubbleBarrierEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        bubble.yRot = yRotationOffset * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        bubble.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}