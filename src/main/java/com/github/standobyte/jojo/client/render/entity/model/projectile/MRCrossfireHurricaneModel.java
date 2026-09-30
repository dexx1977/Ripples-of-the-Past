package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.client.render.FlameModelRenderer;
import com.github.standobyte.jojo.client.render.block.BlockSprites;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.core.Direction;

// Made with Blockbench 3.9.2


public class MRCrossfireHurricaneModel extends EntityModel<MRCrossfireHurricaneEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final FlameModelRenderer ankh;

    public MRCrossfireHurricaneModel() {
        texWidth = 64;
        texHeight = 64;
        ankh = new FlameModelRenderer(this).setFireSprites(
                BlockSprites.MR_FIRE_BLOCK_0::sprite, 
                BlockSprites.MR_FIRE_BLOCK_1::sprite);
        ankh.setPos(0.0F, 0.0F, 0.0F);

        ankh.addFlame(0.0F, -1.0F, 0.0F, 3, 10, Direction.UP);
        ankh.addFlame(0.0F, -14.5F, 0.0F, 3, 8, Direction.WEST);
        ankh.addFlame(0.0F, -14.5F, 0.0F, 3, 8, Direction.EAST);
        ankh.addFlame(4.0F, -16.0F, 0.0F, 2, 8, Direction.UP);
        ankh.addFlame(-5.0F, -17.0F, 0.0F, 2, 8, Direction.WEST);
        ankh.addFlame(-4.0F, -26.0F, 0.0F, 2, 8, Direction.DOWN);
        ankh.addFlame(5.0F, -25.0F, 0.0F, 2, 8, Direction.EAST);
    }

    @Override
    public void setupAnim(MRCrossfireHurricaneEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        ankh.yRot = yRotationOffset * ((float)Math.PI / 180F);
        ankh.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ankh.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}