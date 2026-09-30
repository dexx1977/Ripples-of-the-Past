package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

// Made with Blockbench 4.1.3


public class CDBlockBulletModel extends EntityModel<CDBlockBulletEntity> {
    private final ModelPart heart;
    private final ModelPart leftHalf;
    private final ModelPart rightHalf;

    public CDBlockBulletModel() {
        texWidth = 16;
        texHeight = 16;

        heart = new ModelPart(this);
        heart.setPos(0.0F, 0.0F, 0.0F);
        

        leftHalf = new ModelPart(this);
        leftHalf.setPos(0.0F, 0.5F, 0.0F);
        heart.addChild(leftHalf);
        leftHalf.yRot = 0.7854F;
        leftHalf.texOffs(0, 4).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 1.0F, 3.0F, 0.0F, false);

        rightHalf = new ModelPart(this);
        rightHalf.setPos(0.0F, 0.5F, 0.0F);
        heart.addChild(rightHalf);
        rightHalf.yRot = -0.7854F;
        rightHalf.texOffs(0, 8).addBox(0.0F, -1.0F, 0.0F, 5.0F, 1.0F, 3.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(CDBlockBulletEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        heart.yRot = yRotationOffset * ((float)Math.PI / 180F);
        heart.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        heart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}