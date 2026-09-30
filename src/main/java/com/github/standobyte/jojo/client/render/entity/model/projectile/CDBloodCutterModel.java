package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

// Made with Blockbench 4.1.3


public class CDBloodCutterModel extends EntityModel<CDBloodCutterEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart cutter;

    public CDBloodCutterModel() {
        texWidth = 16;
        texHeight = 16;

        cutter = new ModelPart(this);
        cutter.setPos(0.0F, -3.1F, 0.0F);
        cutter.texOffs(0, 0).addBox(-0.5F, -3.1F, -0.4F, 1.0F, 6.0F, 4.0F, -0.4F, false);
    }

    @Override
    public void setupAnim(CDBloodCutterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        cutter.yRot = yRotationOffset * ((float)Math.PI / 180F);
        cutter.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cutter.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}