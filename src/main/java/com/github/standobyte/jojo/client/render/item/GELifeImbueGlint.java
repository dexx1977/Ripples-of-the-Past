package com.github.standobyte.jojo.client.render.item;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

public abstract class GELifeImbueGlint extends RenderType {
    private static final ResourceLocation GLINT_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/item_imbued_with_life.png");
    
    private static final RenderType GLINT_TRANSLUCENT = create("glint_translucent", DefaultVertexFormat.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderStateShard.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(false));
    private static final RenderType GLINT = create("glint", DefaultVertexFormat.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderStateShard.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false));
    private static final RenderType GLINT_DIRECT = create("glint_direct", DefaultVertexFormat.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderStateShard.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false));
    private static final RenderType ENTITY_GLINT = create("entity_glint", DefaultVertexFormat.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderStateShard.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false));
    private static final RenderType ENTITY_GLINT_DIRECT = create("entity_glint_direct", DefaultVertexFormat.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderStateShard.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false));
    
    private GELifeImbueGlint() {
        super(null, null, 0, 0, false, false, null, null);
    }
    
    
    @Nullable
    public static VertexConsumer overrideVertexBuilder(ItemStack item, PoseStack matrixStack, MultiBufferSource buffer, 
            RenderType renderType, ItemTransforms.ItemDisplayContext transformType, boolean blockSheet) {
        VertexConsumer builder = null;
        boolean goldEFoil = GEItemMarkEffect.isItemMarked(item, Minecraft.getInstance().player);
        if (goldEFoil) {
            if (item.getItem() == Items.COMPASS) {
                matrixStack.pushPose();
                PoseStack.Entry matrixEntry = matrixStack.last();
                if (transformType == ItemTransforms.ItemDisplayContext.GUI) {
                    matrixEntry.pose().multiply(0.5F);
                } else if (transformType.firstPerson()) {
                    matrixEntry.pose().multiply(0.75F);
                }

                if (blockSheet) {
                    builder = getCompassFoilBufferDirect(buffer, renderType, matrixEntry);
                } else {
                    builder = getCompassFoilBuffer(buffer, renderType, matrixEntry);
                }

                matrixStack.popPose();
            } else if (blockSheet) {
                builder = getFoilBufferDirect(buffer, renderType, true);
            } else {
                builder = getFoilBuffer(buffer, renderType, true);
            }
        }
        return builder;
    }
    

    public static RenderType glintTranslucent() {
        return GLINT_TRANSLUCENT;
    }

    public static RenderType glint() {
        return GLINT;
    }

    public static RenderType glintDirect() {
        return GLINT_DIRECT;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType entityGlintDirect() {
        return ENTITY_GLINT_DIRECT;
    }
    
    private static VertexConsumer getFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, boolean pIsItem) {
        return Minecraft.useShaderTransparency() && pRenderType == Sheets.translucentItemSheet() ? 
                VertexBuilderUtils.create(pBuffer.getBuffer(GLINT_TRANSLUCENT), pBuffer.getBuffer(pRenderType))
                : VertexBuilderUtils.create(pBuffer.getBuffer(pIsItem ? glint() : entityGlint()), pBuffer.getBuffer(pRenderType));
    }
    
    private static VertexConsumer getFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, boolean pNoEntity) {
        return VertexBuilderUtils.create(pBuffer.getBuffer(pNoEntity ? glintDirect() : entityGlintDirect()), pBuffer.getBuffer(pRenderType));
    }
    
    private static VertexConsumer getCompassFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Entry pMatrixEntry) {
       return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(pBuffer.getBuffer(glint()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
    }
    
    private static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Entry pMatrixEntry) {
       return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(pBuffer.getBuffer(glintDirect()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
    }
}
