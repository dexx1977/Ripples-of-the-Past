package com.github.standobyte.jojo.client.render.item.tommygun;

import net.minecraft.world.item.ItemDisplayContext;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.item.generic.CustomModelItemISTER;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.TommyGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class TommyGunISTER extends CustomModelItemISTER<TommyGunModel> {
    private final ResourceLocation[] fireTexture = new ResourceLocation[] {
            new ResourceLocation(JojoMod.MOD_ID, "textures/item/tommy_gun_fire_1.png"),
            new ResourceLocation(JojoMod.MOD_ID, "textures/item/tommy_gun_fire_2.png")
    };
            
    
    public TommyGunISTER() {
        super(
                new ResourceLocation(JojoMod.MOD_ID, "tommy_gun"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/item/tommy_gun.png"),
                ModItems.TOMMY_GUN,
                TommyGunModel::new);
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, int overlay) {
        switch (transformType) {
        case GUI:
        case GROUND:
        case FIXED:
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
            CustomModelItemISTER.renderItemNormally(matrixStack, itemStack, transformType, buffer, light, overlay, model);
            break;
        default:
            super.renderByItem(itemStack, transformType, matrixStack, buffer, light, overlay);
            break;
        }
    }
    
    @Override
    protected void doRender(ItemStack itemStack, ItemDisplayContext transformType, PoseStack matrixStack, 
            MultiBufferSource renderTypeBuffer, int light, int overlay) {
        super.doRender(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
        float fire = TommyGunItem.getGunshotTick(itemStack) - ClientUtil.getPartialTick();
        if (fire > 1f) {
            ResourceLocation texture = fireTexture[fire >= 1.5f ? 1 : 0];
            VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(model.renderType(texture));
            model.renderFire(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
