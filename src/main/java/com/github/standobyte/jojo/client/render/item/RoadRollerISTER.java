package com.github.standobyte.jojo.client.render.item;

import com.github.standobyte.jojo.client.render.entity.renderer.RoadRollerRenderer;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RoadRollerISTER extends BlockEntityWithoutLevelRenderer {
    private final RoadRollerItemModel roadRollerModel = new RoadRollerItemModel();

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.ItemDisplayContext transformType, PoseStack matrixStack, 
            MultiBufferSource renderTypeBuffer, int light, int overlay) {
        Item item = itemStack.getItem();
        if (item == ModItems.ROAD_ROLLER.get()) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
                    renderTypeBuffer, roadRollerModel.renderType(RoadRollerRenderer.TEXTURE), false, itemStack.hasFoil());
            roadRollerModel.renderToBuffer(matrixStack, vertexBuilder, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        }
    }
}
