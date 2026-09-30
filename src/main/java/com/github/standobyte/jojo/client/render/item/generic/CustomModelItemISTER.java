package com.github.standobyte.jojo.client.render.item.generic;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.resources.models.ResourceEntityModels;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;

public class CustomModelItemISTER<M extends Model> extends BlockEntityWithoutLevelRenderer implements ISTERWithEntity {
    public final ResourceLocation modelResource;
    public final ResourceLocation texture;
    protected final Supplier<? extends Item> item;
    protected final Supplier<M> modelObjConstructor;
    public M model;
    @Nullable protected LivingEntity entity;
    
    public CustomModelItemISTER(ResourceLocation modelResource, ResourceLocation texture, 
            Supplier<? extends Item> item, Supplier<M> modelObjConstructor) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.modelResource = modelResource;
        this.texture = texture;
        this.item = item;
        this.modelObjConstructor = modelObjConstructor;
        ResourceEntityModels.addModelLoader(modelResource, modelObjConstructor, newModel -> this.model = newModel);
    }
    
    @Override
    public void setEntity(@Nullable LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack matrixStack, 
            MultiBufferSource renderTypeBuffer, int light, int overlay) {
        Item item = itemStack.getItem();
        if (item == this.item.get()) {
            if (model != null) {
                matrixStack.pushPose();
                matrixStack.scale(-1.0F, -1.0F, 1.0F);
                matrixStack.translate(-0.5, -1.5, 0.5);
                doRender(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
                matrixStack.popPose();
            }
            else {
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                BakedModel missingModel = itemRenderer.getItemModelShaper().getModelManager().getMissingModel();
                VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
                        renderTypeBuffer, ItemBlockRenderTypes.getRenderType(itemStack, true), 
                        false, itemStack.hasFoil());
                itemRenderer.renderModelLists(missingModel, itemStack, light, overlay, matrixStack, vertexBuilder);
            }
        }
    }
    
    protected void doRender(ItemStack itemStack, ItemDisplayContext transformType, PoseStack matrixStack, 
            MultiBufferSource renderTypeBuffer, int light, int overlay) {
        VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
                renderTypeBuffer, model.renderType(texture), false, itemStack.hasFoil());
        model.renderToBuffer(matrixStack, vertexBuilder, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    
    
    public static void renderItemNormally(PoseStack matrixStack, ItemStack itemStack, ItemDisplayContext transformType, 
            MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel itemModel) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean cull;
        if (transformType != ItemDisplayContext.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem) {
            Block block = ((BlockItem)itemStack.getItem()).getBlock();
            cull = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
            cull = true;
        }
        // 1.20.1 renders a model once per render type, which covers layered models too
        for (RenderType renderType : itemModel.getRenderTypes(itemStack, cull)) {
            VertexConsumer vertexBuilder;
            if (cull) {
                vertexBuilder = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, itemStack.hasFoil());
            } else {
                vertexBuilder = ItemRenderer.getFoilBuffer(buffer, renderType, true, itemStack.hasFoil());
            }

            itemRenderer.renderModelLists(itemModel, itemStack, combinedLight, combinedOverlay, matrixStack, vertexBuilder);
        }
    }
}
