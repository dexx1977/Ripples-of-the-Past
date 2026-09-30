package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonCutterEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

// Made with Blockbench 3.9.2


public class HamonCutterModel extends EntityModel<HamonCutterEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart cutter;

    public HamonCutterModel() {
        texWidth = 32;
        texHeight = 32;
        cutter = new ModelPart(this);
        cutter.setPos(0.0F, 0.0F, 0.0F);
        cutter.texOffs(0, 0).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 1.0F, 6.0F, -0.4F, false);
        cutter.texOffs(0, 7).addBox(-3.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F, -0.395F, false);
    }

    @Override
    public void setupAnim(HamonCutterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        yRotationOffset = (yRotationOffset + ticks * 60F) % 360F;
        cutter.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cutter.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}