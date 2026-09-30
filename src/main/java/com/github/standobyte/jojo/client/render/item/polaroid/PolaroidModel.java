package com.github.standobyte.jojo.client.render.item.polaroid;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class PolaroidModel extends Model {
    private ModelPart polaroid;
    private ModelPart flash;
    private ModelPart photo;

    public PolaroidModel() {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (polaroid != null) {
            polaroid.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (photo != null) {
            photo.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
    
    
    public void setRenderPhoto(boolean renderPhoto) {
        if (polaroid != null) {
            polaroid.visible = !renderPhoto;
        }
        if (photo != null) {
            photo.visible = renderPhoto;
        }
    }
    
    public void setAnim(boolean open, float photoProgress) {
        if (flash != null) {
            flash.xRot = open ? (float) -Math.PI / 2 : 0;
        }
        
        if (photo != null) {
            photo.z = 1.25f - photoProgress * 8;
            
            float rot = photoProgress < 0.6f ? 0 : (photoProgress - 0.6f) * 2.5f;
            photo.xRot = 10 * MathUtil.DEG_TO_RAD * rot;
        }
    }
}
