package com.github.standobyte.jojo.client.render.item.standdisc;

import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class StandDiscISTER extends BlockEntityWithoutLevelRenderer {

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.ItemDisplayContext transformType, 
            PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        BakedModel pModel = ir.getModel(itemStack, null, null);
        
        RenderType rendertype = ItemBlockRenderTypes.getRenderType(itemStack, true);
        VertexConsumer ivertexbuilder = ItemRenderer.getFoilBufferDirect(
                buffer, rendertype, true, itemStack.hasFoil());
        ir.renderModelLists(pModel, itemStack, light, overlay, matrixStack, ivertexbuilder);
        
        
        
        StandInstance stand = StandDiscItem.getStandFromStack(itemStack);
        if (stand != null) {
            renderStandIcon(matrixStack, stand, itemStack, buffer, light, overlay);
        }
    }
    
    
    
    private void renderStandIcon(PoseStack matrixStack, StandInstance stand, ItemStack discItem, 
            MultiBufferSource buffer, int light, int overlay) {
        ResourceLocation icon = StandSkinsManager.getInstance().getRemappedResPath(
                manager -> manager.getStandSkin(stand), stand.getType().getIconTexture(null));
        
        VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
                buffer, RenderType.entityCutoutNoCull(icon), 
                false, discItem.hasFoil());
        
        renderIconQuad(matrixStack.last(), QUAD_FRONT, vertexBuilder, light, overlay);
        renderIconQuad(matrixStack.last(), QUAD_BACK, vertexBuilder, light, overlay);
    }
    
    static {
        float x0 = 1;
        float y0 = 5;
        float z0 = 7.498F;
        float x1 = 7;
        float y1 = 11;
        float z1 = 8.502F;
        ModelPart.PositionTextureVertex vertex7 = new ModelPart.PositionTextureVertex(
                x0, y0, z0, 0.0F, 0.0F);
        ModelPart.PositionTextureVertex vertex = new ModelPart.PositionTextureVertex(
                x1, y0, z0, 0.0F, 8.0F);
        ModelPart.PositionTextureVertex vertex1 = new ModelPart.PositionTextureVertex(
                x1, y1, z0, 8.0F, 8.0F);
        ModelPart.PositionTextureVertex vertex2 = new ModelPart.PositionTextureVertex(
                x0, y1, z0, 8.0F, 0.0F);
        ModelPart.PositionTextureVertex vertex3 = new ModelPart.PositionTextureVertex(
                x0, y0, z1, 0.0F, 0.0F);
        ModelPart.PositionTextureVertex vertex4 = new ModelPart.PositionTextureVertex(
                x1, y0, z1, 0.0F, 8.0F);
        ModelPart.PositionTextureVertex vertex5 = new ModelPart.PositionTextureVertex(
                x1, y1, z1, 8.0F, 8.0F);
        ModelPart.PositionTextureVertex vertex6 = new ModelPart.PositionTextureVertex(
                x0, y1, z1, 8.0F, 0.0F);
        
        QUAD_FRONT = new ModelPart.Polygon(
                new ModelPart.PositionTextureVertex[]{
                        vertex2, 
                        vertex1,
                        vertex, 
                        vertex7 
                }, 
                0, 0, 16, 16, 
                16, 16, false, Direction.NORTH);
        
        QUAD_BACK = new ModelPart.Polygon(
                new ModelPart.PositionTextureVertex[]{ 
                        vertex5, 
                        vertex6,
                        vertex3, 
                        vertex4}, 
                0, 0, 16, 16, 
                16, 16, false, Direction.SOUTH);
    }
    
    private static final Polygon QUAD_FRONT;
    private static final Polygon QUAD_BACK;
    
    private void renderIconQuad(PoseStack.Entry poseEntry, Polygon quad, 
            VertexConsumer vertexBuilder, int light, int overlay) {
        Matrix4f pose = poseEntry.pose();
        Matrix3f entry = poseEntry.normal();
        Vector3f normal = quad.normal.copy();
        normal.transform(entry);
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();

        for (int i = 0; i < quad.vertices.length; ++i) {
            ModelPart.PositionTextureVertex vertex = quad.vertices[i];
            float vertexX = vertex.pos.x() / 16.0F;
            float vertexY = vertex.pos.y() / 16.0F;
            float vertexZ = vertex.pos.z() / 16.0F;
            Vector4f vector4f = new Vector4f(vertexX, vertexY, vertexZ, 1.0F);
            vector4f.transform(pose);
            vertexBuilder.vertex(vector4f.x(), vector4f.y(), vector4f.z(), 
                    1, 1, 1, 1, vertex.u, vertex.v, 
                    overlay, light, x, y, z);
        }
    }
}
