package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.BlockShardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

public class BlockShardRenderer extends EntityRenderer<BlockShardEntity> {

    public BlockShardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockShardEntity pEntity) {
        BlockState block = pEntity.getBlock();
        if (block != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(block);
            if (sprite != null) {
                return CDBlockBulletRenderer.getSpriteTexture(sprite).orElse(InventoryMenu.BLOCK_ATLAS);
            }
        }
        return InventoryMenu.BLOCK_ATLAS;
    }
    
    @Override
    public void render(BlockShardEntity entity, float yRotation, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            BlockState blockState = entity.getBlock();
            if (blockState != null) {
                matrixStack.pushPose();
                matrixStack.scale(-1, -1, 1);
                ModelPart model = getModelRenderer(entity);
                rotate(entity, model, partialTick);
                
                // falling block entity rendering
//                BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//                for (RenderType renderType : RenderType.chunkBufferLayers()) {
//                    if (RenderTypeLookup.canRenderInLayer(blockState, renderType)) {
//                        ForgeHooksClient.setRenderLayer(renderType);
////                        blockRenderer.getModelRenderer().tesselateBlock(entity.level, 
////                                blockRenderer.getBlockModel(blockState), blockState, 
////                                blockpos, pMatrixStack, pBuffer.getBuffer(renderType), false, new Random(), 
////                                blockState.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY);
//                    }
//                }
//                ForgeHooksClient.setRenderLayer(null);
                
                for (RenderType blockRenderType : RenderType.chunkBufferLayers()) {
                    if (blockRenderType == ItemBlockRenderTypes.getChunkRenderType(blockState)) {
                        // FIXME temporary block shard rendering, rewrite to use the atlas sprite
                        ResourceLocation texture = getTextureLocation(entity);
                        if (texture != InventoryMenu.BLOCK_ATLAS) {
                            RenderType renderType = null;
                            if (blockRenderType == RenderType.solid()) {
                                renderType = RenderType.entitySolid(texture);
                            }
                            else if (blockRenderType == RenderType.cutout() || blockRenderType == RenderType.cutoutMipped()) {
                                renderType = RenderType.entityCutout(texture);
                            }
                            else if (blockRenderType == RenderType.translucent()) {
                                renderType = RenderType.entityTranslucent(texture);
                            }
                            if (renderType != null) {
                                VertexConsumer vertexBuilder = buffer.getBuffer(renderType);
                                model.render(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY);
                            }
                        }
                    }
                }
                
                matrixStack.popPose();
            }
        }
    }
    
    private ModelPart[] models = new ModelPart[64];
    private ModelPart[] modelsGlass = new ModelPart[64];
    private ModelPart getModelRenderer(BlockShardEntity entity) {
        long randomNum = entity.getUUID().getMostSignificantBits();
        int x = (int) (randomNum & 3);
        randomNum >>= 2;
        int y = (int) (randomNum & 3);
        randomNum >>= 2;
        int z = (int) (randomNum & 3);
        int index = x * 16 + y * 4 + z;
        
        ModelPart[] cache = entity.isGlass() ? modelsGlass : models;
        if (cache[index] == null) {
            int uv = (int) ((randomNum >> 27) & 255);
            int u = uv & 15;
            int v = (uv >> 4) & 15;
            ModelPart model = new ModelPart(16, 16, u, v);
            
            float xSize = x + 5;
            float ySize = y + 5;
            float zSize = entity.isGlass() ? 1 : z + 5;
            model.addBox(-xSize / 2, -ySize / 2, -zSize / 2, xSize, ySize, zSize);
            model.y = -3;
            cache[index] = model;
        }
        return cache[index];
    }
    
    private void rotate(BlockShardEntity entity, ModelPart modelRenderer, float partialTick) {
        long randomNum = entity.getUUID().getMostSignificantBits() >> 6;
        modelRenderer.xRot = (float) Math.PI * (int) (randomNum & 127) / 64;
        randomNum >>= 7;
        modelRenderer.yRot = (float) Math.PI * (int) (randomNum & 127) / 64;
        randomNum >>= 7;
        modelRenderer.zRot = (float) Math.PI * (int) (randomNum & 127) / 64;
    }

}
