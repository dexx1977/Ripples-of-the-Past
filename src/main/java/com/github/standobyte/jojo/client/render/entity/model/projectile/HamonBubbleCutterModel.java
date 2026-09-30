package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

// Made with Blockbench 3.9.2


public class HamonBubbleCutterModel extends EntityModel<HamonBubbleCutterEntity> {
    private final ModelPart cutter;

    public HamonBubbleCutterModel() {
        texWidth = 32;
        texHeight = 32;
        cutter = new ModelPart(this);
        cutter.setPos(0.0F, 0.0F, 0.0F);
        cutter.texOffs(0, 0).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 1.0F, 6.0F, -0.4F, false);
        cutter.texOffs(0, 7).addBox(-3.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F, -0.395F, false);
        cutter.texOffs(0, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.2F, false);
    }

    @Override
    public void setupAnim(HamonBubbleCutterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        yRotationOffset = (yRotationOffset + ticks * 60F) % 360F;
        cutter.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cutter.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}