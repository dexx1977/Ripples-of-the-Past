package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.action.stand.GoldExperienceMarkItem;
import com.github.standobyte.jojo.client.particle.custom.FirstPersonHamonAura;
import com.github.standobyte.jojo.client.render.item.InventoryItemHighlight;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void jojoOnItemRender(ItemStack pItemStack, ItemTransforms.ItemDisplayContext pTransformType, boolean pLeftHand, 
            PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        switch (pTransformType) {
        case FIRST_PERSON_LEFT_HAND:
            render1stPersonHamonAura(pMatrixStack, pBuffer, pItemStack, HumanoidArm.LEFT);
            break;
        case FIRST_PERSON_RIGHT_HAND:
            render1stPersonHamonAura(pMatrixStack, pBuffer, pItemStack, HumanoidArm.RIGHT);
            break;
        default:
            break;
        }
    }
    
    @ModifyVariable(method = "render", at = @At(value = "STORE"))
    public VertexConsumer changeVertexBuilder(VertexConsumer vertexBuilder, 
            ItemStack pItemStack, ItemTransforms.ItemDisplayContext pTransformType, boolean pLeftHand, 
            PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel) {
        boolean flag1;
        if (pTransformType != ItemTransforms.ItemDisplayContext.GUI && !pTransformType.firstPerson() && pItemStack.getItem() instanceof BlockItem) {
           Block block = ((BlockItem)pItemStack.getItem()).getBlock();
           flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
           flag1 = true;
        }
        RenderType rendertype = ItemBlockRenderTypes.getRenderType(pItemStack, flag1);
        PoseStack.Entry matrixstack$entry = pMatrixStack.last();
        
        return GoldExperienceMarkItem.ClientStuff.qwe(vertexBuilder, pItemStack, flag1, pBuffer, rendertype, matrixstack$entry);
    }
    
    private static void render1stPersonHamonAura(PoseStack matrixStack, MultiBufferSource buffer, ItemStack itemStack, HumanoidArm handSide) {
        if (!MCUtil.itemHandFree(itemStack)) {
            matrixStack.pushPose();
            FirstPersonHamonAura.itemMatrixTransform(matrixStack, handSide, itemStack);
            FirstPersonHamonAura.getInstance().renderParticles(matrixStack, buffer, handSide);
            matrixStack.popPose();
        }
    }
    
    @ModifyVariable(method = "render", remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 1)
    public int jojoItemHighlight(int pCombinedOverlay, ItemStack pItemStack, ItemTransforms.ItemDisplayContext pTransformType, boolean pLeftHand, 
            PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlayArg, BakedModel pModel) {
        if (!pItemStack.isEmpty()) {
            float partialTick = Minecraft.getInstance().getDeltaFrameTime();
            float overlayAmount = InventoryItemHighlight.getHighlightAmount(pItemStack.getItem(), partialTick);
            if (overlayAmount >= 0) {
                int highlight = OverlayTexture.pack(OverlayTexture.u(overlayAmount), OverlayTexture.v(false));
                return highlight;
            }
        }
        
        return pCombinedOverlay;
    }
    
}
