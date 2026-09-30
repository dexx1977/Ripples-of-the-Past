package com.github.standobyte.jojo.client.render.item.tommygun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class TommyGunModel extends Model {
    private ModelPart tommyGun;
    private ModelPart fire;

    public TommyGunModel() {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (tommyGun != null) {
            tommyGun.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
    
    public void renderFire(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (fire != null) {
            fire.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

}
