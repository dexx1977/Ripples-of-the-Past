package com.github.standobyte.jojo.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import com.mojang.math.Axis;

public class CustomIconMapRender {
    private static PoseStack matrixStack;
    private static MultiBufferSource buffer;
    @SuppressWarnings("unused")
    private static boolean active;
    private static int packedLight;
    
    public static void clCaptureIconRenderArgs(
            PoseStack pMatrixStack, MultiBufferSource pBuffer, 
            boolean pActive, int pPackedLight) {
        matrixStack = pMatrixStack;
        buffer = pBuffer;
        active = pActive;
        packedLight = pPackedLight;
    }
    
    public static void customIconRender(MapDecoration mapIcon, ResourceLocation iconPath, int index) {
        RenderType icon = RenderType.text(iconPath);
        
        if (matrixStack == null || buffer == null) {
            return;
        }
        
        matrixStack.pushPose();
        matrixStack.translate(((float)mapIcon.getX() / 2 + 64), ((float)mapIcon.getY() / 2 + 64), -0.02);
        matrixStack.mulPose(Axis.ZP.rotationDegrees((float)(mapIcon.getRot() * 360) / 16));
        matrixStack.scale(4, 4, 3);
        matrixStack.translate(-0.125, 0.125, 0);
        Matrix4f matrix4f1 = matrixStack.last().pose();
        VertexConsumer ivertexbuilder1 = buffer.getBuffer(icon);
        ivertexbuilder1.vertex(matrix4f1, -2,  2, (float)index * -0.001F).color(255, 255, 255, 255).uv(1, 0).uv2(packedLight).endVertex();
        ivertexbuilder1.vertex(matrix4f1,  2,  2, (float)index * -0.001F).color(255, 255, 255, 255).uv(0, 0).uv2(packedLight).endVertex();
        ivertexbuilder1.vertex(matrix4f1,  2, -2, (float)index * -0.001F).color(255, 255, 255, 255).uv(0, 1).uv2(packedLight).endVertex();
        ivertexbuilder1.vertex(matrix4f1, -2, -2, (float)index * -0.001F).color(255, 255, 255, 255).uv(1, 1).uv2(packedLight).endVertex();
        matrixStack.popPose();
    }
}
