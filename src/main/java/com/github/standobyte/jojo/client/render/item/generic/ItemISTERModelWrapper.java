package com.github.standobyte.jojo.client.render.item.generic;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;

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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("deprecation")
public class ItemISTERModelWrapper implements BakedModel {
    private final BakedModel existingModel;
    private ISTERItemCaptureEntity captureEntityOverrides = null;
    private ItemOverrides wrappedOverrides = null;
    
    public ItemISTERModelWrapper(BakedModel existingModel) {
        this.existingModel = existingModel;
    }
    
    public ItemISTERModelWrapper setCaptureEntity() {
        captureEntityOverrides = new ISTERItemCaptureEntity();
        return this;
    }
    
    public ItemISTERModelWrapper refreshOverrides(Map<ResourceLocation, BakedModel> registry) {
        // 1.20.1 bakes the override models into final fields of the override list, so
        // the overrides are resolved through this wrapper instead of being replaced
        ItemOverrides overridesList = existingModel.getOverrides();
        if (overridesList != null && overridesList != ItemOverrides.EMPTY) {
            this.wrappedOverrides = new WrappingOverrides(overridesList, this);
        }
        return this;
    }
    
    /** Wraps the model the base overrides resolve to, so the overrides use the ISTER too. */
    private static class WrappingOverrides extends ItemOverrides {
        private final ItemOverrides wrapped;
        private final ItemISTERModelWrapper parent;
        
        private WrappingOverrides(ItemOverrides wrapped, ItemISTERModelWrapper parent) {
            this.wrapped = wrapped;
            this.parent = parent;
        }
        
        @Override
        public BakedModel resolve(BakedModel model, ItemStack item, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            BakedModel resolved = wrapped.resolve(model, item, world, entity, seed);
            if (resolved == null || resolved instanceof ItemISTERModelWrapper) {
                return resolved;
            }
            ItemISTERModelWrapper wrapper = new ItemISTERModelWrapper(resolved);
            if (parent.captureEntityOverrides != null) {
                wrapper.setCaptureEntity();
            }
            return wrapper;
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @Nonnull RandomSource rand) {
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
        if (wrappedOverrides != null) {
            return wrappedOverrides;
        }
        return this.existingModel.getOverrides();
    }

}
