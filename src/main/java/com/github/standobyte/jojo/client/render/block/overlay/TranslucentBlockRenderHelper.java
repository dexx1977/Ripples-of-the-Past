package com.github.standobyte.jojo.client.render.block.overlay;

import net.minecraftforge.client.model.data.ModelData;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.rendertype.ModifiedRenderType;
import com.github.standobyte.jojo.client.render.rendertype.ModifiedRenderTypeBuffers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;

// A helper class for rendering translucent blocks overlay
// as a quality-of-life feature for Crazy Diamond's terrain restoration ability

// The original idea of remapping render types is taken from
// MultiblockVisualizationHandler class from Vazkii's Patchouli mod
// (licensed under CC BY-NC-SA 3.0)
public class TranslucentBlockRenderHelper {
    private static MultiBufferSource.BufferSource buffers = null;

    public static void renderCDRestorationTranslucentBlocks(PoseStack matrixStack, Minecraft mc, 
            Stream<PrevBlockInfo> blocks, Predicate<PrevBlockInfo> inAbilityRange) {
        if (buffers == null) {
            buffers = ModifiedRenderTypeBuffers.create(
                    mc.renderBuffers().bufferSource(), 
                    renderType -> new ModifiedRenderType(renderType, 
                            () -> {
                                RenderSystem.disableDepthTest();
                                RenderSystem.enableBlend();
                                RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                                RenderSystem.blendColor(1, 1, 1, 0.3f);
                            }, 
                            () -> {
                                RenderSystem.blendColor(1, 1, 1, 1);
                                RenderSystem.defaultBlendFunc();
                                RenderSystem.disableBlend();
                                RenderSystem.enableDepthTest();
                            },
                            "cd_blocks"));
        }
        
        Camera renderInfo = mc.gameRenderer.getMainCamera();
        Vec3 projectedView = renderInfo.getPosition();
        matrixStack.pushPose();
        matrixStack.translate(
                -projectedView.x(), 
                -projectedView.y(), 
                -projectedView.z());

        BlockRenderDispatcher renderer = mc.getBlockRenderer();
        int overlayTexture = OverlayTexture.pack(Math.abs((int) (Util.getMillis() % 2000) / 100 - 10), 10);
        blocks.forEach(block -> {
            BlockPos pos = block.pos;
            BlockState blockState = block.state;
            IModelData tileData = ModelDataManager.getModelData(mc.level, pos);
            if (tileData == null) tileData = ModelData.EMPTY;
            IModelData model = renderer.getBlockModel(blockState).getModelData(mc.level, pos, blockState, tileData);
            matrixStack.pushPose();
            matrixStack.translate(
                    pos.getX(), 
                    pos.getY(), 
                    pos.getZ());
            int overlay = inAbilityRange.test(block) ? overlayTexture : OverlayTexture.NO_OVERLAY;
            
            RenderShape renderType = blockState.getRenderShape();
            if (renderType == RenderShape.MODEL) {
                BakedModel bakedModel = renderer.getBlockModel(blockState);
                int color = mc.getBlockColors().getColor(blockState, mc.level, pos, 0);
                float[] rgb = ClientUtil.rgb(color);
                renderer.getModelRenderer().renderModel(matrixStack.last(), buffers.getBuffer(ItemBlockRenderTypes.getRenderType(blockState, false)), 
                        blockState, bakedModel, rgb[0], rgb[1], rgb[2], 0xF000F0, overlay, model);
            }
            
            matrixStack.popPose();
        });

        buffers.endBatch();
        matrixStack.popPose();
    }
}
