package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;
import java.util.function.Function;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public abstract class StandModelLayerRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends RenderLayer<T, M> {
    protected final StandEntityRenderer<T, M> entityRenderer;
    protected final boolean useParentModel;
    protected final M model;
    protected final ResourceLocation texture;

    public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer, M model, ResourceLocation texture) {
        this(entityRenderer, false, model, texture);
    }

    public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer, ResourceLocation texture) {
        this(entityRenderer, true, null, texture);
    }

    public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer, boolean useParentModel, M model, ResourceLocation texture) {
        super(entityRenderer);
        this.entityRenderer = (StandEntityRenderer<T, M>) entityRenderer;
        this.model = model;
        this.texture = texture;
        this.useParentModel = useParentModel;
        if (model != null) {
            model.setAnimatorSupplier(getParentModel().getGeckoAnimator());
            if (!useParentModel) {
                model.afterInit();
            }
        }
    }

    public M getLayerModel(T entity) {
        return getLayerModel(entity.getStandSkin());
    }

    public M getLayerModel(Optional<ResourceLocation> standSkin) {
        if (useParentModel) {
            return entityRenderer.getModel(standSkin);
        }
        if (this.model != null) {
            M model = CustomResources.getStandModelOverrides().overrideModel(this.model);
            M skinModel = StandSkinsManager.getInstance().getStandSkin(standSkin).map(
                    skin -> (M) skin.standModels.getOrDefault(model.getModelId(), model)).orElse(model);
            return skinModel;
        }
        return null;
    }
    
    public boolean shouldRender(T entity, Optional<ResourceLocation> standSkin) {
        return true;
    }

    public int getPackedLight(int packedLight) {
        return packedLight;
    }
    
    public RenderType getRenderType(T entity) {
        return entityRenderer.getRenderType(entity, getLayerModel(Optional.empty()), getLayerTexture(entity.getStandSkin()));
    }
    
    public RenderType getRenderType(T entity, Function<ResourceLocation, RenderType> renderTYPE) {
        return renderTYPE.apply(getLayerTexture(entity.getStandSkin()));
    }
    
    public ResourceLocation getBaseTexture() {
        return texture;
    }

    public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(standSkin), texture);
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight,
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
            float ticks, float headYRotation, float headXRotation) {
        RenderType renderType = getRenderType(entity);
        if (renderType != null) {
            render(matrixStack, buffer, renderType, packedLight, 
                    entity, walkAnimPos, walkAnimSpeed, partialTick, 
                    ticks, headYRotation, headXRotation);
        }
    }
    
    public void render(PoseStack matrixStack, MultiBufferSource buffer, RenderType renderType, int packedLight,
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
            float ticks, float headYRotation, float headXRotation) {
        if (renderType != null && shouldRender(entity, entity.getStandSkin())) {
            M layerModel = getLayerModel(entity);
            M parentModel = entityRenderer.getModel(entity);
            layerModel.idleLoopTickStamp = parentModel.idleLoopTickStamp;
            entityRenderer.renderLayer(matrixStack, buffer.getBuffer(renderType), getPackedLight(packedLight), 
                    entity, walkAnimPos, walkAnimSpeed, partialTick, 
                    ticks, headYRotation, headXRotation, layerModel);
        }
    }
}
