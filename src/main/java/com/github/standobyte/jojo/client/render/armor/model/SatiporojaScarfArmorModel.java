package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.LivingEntity;

// Made with Blockbench 3.9.2


public class SatiporojaScarfArmorModel extends HumanoidModel<LivingEntity> {

    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    public SatiporojaScarfArmorModel(float size) {
        super(size);
        texWidth = 32;
        texHeight = 32;
        head.cubes.clear();
        head.setPos(0.0F, 0.5F, 0.0F);
        setRotationAngle(head, 0.0873F, 0.0F, 0.0F);
        // a baked vanilla part has no texture offset setter, so the scarf is built
        // with the model part helper and its cuboids take the head's place
        ModelPart scarfHead = new ModelPart(this);
        scarfHead.setTexSize(texWidth, texHeight);
        scarfHead.texOffs(0, 7).addBox(-4.5F, -1.2F, -2.5F, 9.0F, 1.0F, 5.0F, 0.0F, false);
        scarfHead.texOffs(0, 0).addBox(-4.5F, 0.0F, -2.6F, 9.0F, 2.0F, 5.0F, 0.2F, false);
        scarfHead.texOffs(0, 13).addBox(-4.1F, -0.5F, -3.5F, 3.0F, 11.0F, 1.0F, -0.3F, false);
        head.cubes.clear();
        head.cubes.addAll(scarfHead.cubesMutable());
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return Collections.emptyList();
    }
    
    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(head);
    }
    
    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        setRotationAngle(head, body.xRot + 0.0873F, body.yRot, body.zRot);
        super.renderToBuffer(matrixStack, vertexBuilder, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(net.minecraft.client.model.geom.ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}