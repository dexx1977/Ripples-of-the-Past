package com.github.standobyte.jojo.client.render.item.generic;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("deprecation")
public class ItemISTERModelWrapper implements BakedModel {
    private BakedModel existingModel;
    private ISTERItemCaptureEntity captureEntityOverrides = null;
    
    public ItemISTERModelWrapper(BakedModel existingModel) {
        this.existingModel = existingModel;
    }
    
    public ItemISTERModelWrapper setCaptureEntity() {
        captureEntityOverrides = new ISTERItemCaptureEntity();
        return this;
    }
    
    public ItemISTERModelWrapper refreshOverrides(Map<ResourceLocation, BakedModel> registry) {
        ItemOverrides overridesList = existingModel.getOverrides();
        if (overridesList != null) {
            List<ItemOverride> overrides = ClientReflection.getOverrides(overridesList);
            if (!overrides.isEmpty()) {
                List<BakedModel> overrideModels = ClientReflection.getOverrideModels(overridesList);
                for (int i = 0; i < overrides.size(); i++) {
                    ItemOverride override = overrides.get(i);
                    ResourceLocation key = override.getModel();
                    key = new ModelResourceLocation(new ResourceLocation(key.getNamespace(), key.getPath().replace("item/", "")), "inventory");
                    BakedModel replacementModel = registry.get(key);
                    if (replacementModel != null) {
                        overrideModels.set(i, replacementModel);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @Nonnull Random rand) {
        return this.existingModel.getQuads(state, direction, rand);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.existingModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return this.existingModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.existingModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.existingModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        if (captureEntityOverrides != null) {
            return captureEntityOverrides;
        }
        return this.existingModel.getOverrides();
    }

}
