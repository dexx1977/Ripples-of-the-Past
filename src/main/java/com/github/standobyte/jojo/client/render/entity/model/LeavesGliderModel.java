package com.github.standobyte.jojo.client.render.entity.model;

import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

// Made with Blockbench 3.9.3


public class LeavesGliderModel extends EntityModel<LeavesGliderEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart glider;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;

    public LeavesGliderModel() {
        texWidth = 16;
        texHeight = 16;

        glider = new ModelPart(this);
        glider.setPos(0.0F, 0.0F, 0.0F);
        glider.texOffs(0, 0).addBox(-20.0F, -0.5F, -4.11F, 40.0F, 1.0F, 24.0F, -0.375F, false);
        
        frontLeft = new ModelPart(this);
        frontLeft.setPos(0.0F, 0.0F, -19.625F);
        glider.addChild(frontLeft);
        setRotationAngle(frontLeft, 0.0F, -0.6806F, 0.0F);
        frontLeft.texOffs(0, 25).addBox(-0.375F, -0.5F, -0.375F, 26.0F, 1.0F, 16.0F, -0.375F, false);

        frontRight = new ModelPart(this);
        frontRight.setPos(0.0F, 0.0F, -19.625F);
        glider.addChild(frontRight);
        setRotationAngle(frontRight, 0.0F, 0.6806F, 0.0F);
        frontRight.texOffs(0, 42).addBox(-25.625F, -0.5F, -0.375F, 26.0F, 1.0F, 16.0F, -0.375F, false);
    }

    @Override
    public void setupAnim(LeavesGliderEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        glider.yRot = yRotationOffset * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        glider.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}