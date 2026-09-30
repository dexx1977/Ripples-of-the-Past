package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class SCRapierModel extends EntityModel<SCRapierEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart rapier;

    public SCRapierModel() {
        texWidth = 128;
        texHeight = 128;
        rapier = new ModelPart(this);
        rapier.setPos(-0.5F, 0.0F, 0.0F);
        rapier.texOffs(32, 72).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 15.0F, -0.3F, false);
    }

    @Override
    public void setupAnim(SCRapierEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        rapier.yRot = yRotationOffset * ((float)Math.PI / 180F);
        rapier.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        rapier.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}