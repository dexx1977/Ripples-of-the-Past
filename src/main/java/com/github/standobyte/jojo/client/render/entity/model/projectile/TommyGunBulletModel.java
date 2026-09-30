package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class TommyGunBulletModel extends EntityModel<TommyGunBulletEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart bullet;

    public TommyGunBulletModel() {
        texWidth = 8;
        texHeight = 8;
        bullet = new ModelPart(this);
        bullet.setPos(0.0F, -0.5F, 0.0F);
        bullet.texOffs(0, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 2.0F, -0.1F, false);
        bullet.texOffs(0, 3).addBox(-0.5F, -0.5F, 1.3F, 1.0F, 1.0F, 1.0F, -0.2F, false);
        bullet.texOffs(0, 0).addBox(-0.5F, -0.5F, 2.1F, 1.0F, 1.0F, 0.0F, -0.1F, false);
    }

    @Override
    public void setupAnim(TommyGunBulletEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        bullet.yRot = yRotationOffset * ((float)Math.PI / 180F);
        bullet.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bullet.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
