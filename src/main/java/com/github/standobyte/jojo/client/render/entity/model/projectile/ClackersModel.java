package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

// Made with Blockbench 3.9.2


public class ClackersModel extends EntityModel<ClackersEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart clackers;
    private final ModelPart string1;
    private final ModelPart ball1;
    private final ModelPart string2;
    private final ModelPart ball2;

    public ClackersModel() {
        texWidth = 32;
        texHeight = 32;

        clackers = new ModelPart(this);
        clackers.setPos(0.0F, -4.0F, 0.0F);
        clackers.texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, -0.25F, false);

        string1 = new ModelPart(this);
        string1.setPos(0.0F, 0.0F, 0.0F);
        clackers.addChild(string1);
        string1.texOffs(24, 6).addBox(-0.5F, -7.925F, -0.5F, 1.0F, 8.0F, 1.0F, -0.075F, false);

        ball1 = new ModelPart(this);
        ball1.setPos(0.0F, -7.85F, 0.0F);
        string1.addChild(ball1);
        ball1.texOffs(0, 0).addBox(-3.0F, -4.5F, -3.0F, 6.0F, 6.0F, 6.0F, -1.5F, false);

        string2 = new ModelPart(this);
        string2.setPos(0.0F, 0.0F, 0.0F);
        clackers.addChild(string2);
        string2.texOffs(28, 6).addBox(-0.5F, -0.075F, -0.5F, 1.0F, 8.0F, 1.0F, -0.075F, false);

        ball2 = new ModelPart(this);
        ball2.setPos(0.0F, 7.55F, 0.0F);
        string2.addChild(ball2);
        ball2.texOffs(0, 12).addBox(-3.0F, -1.2F, -3.0F, 6.0F, 6.0F, 6.0F, -1.5F, false);
    }

    @Override
    public void setupAnim(ClackersEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        clackers.setPos(0.0F, -4.0F, 0.0F);
        if (!entity.isInGround()) {
            xRotation = (xRotation + ticks * 18.0F * (float) entity.getDeltaMovement().length()) % 360.0F;
        }
        clackers.xRot = xRotation * MathUtil.DEG_TO_RAD;
        clackers.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        clackers.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}