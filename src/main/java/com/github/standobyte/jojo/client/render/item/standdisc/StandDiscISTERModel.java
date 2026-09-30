package com.github.standobyte.jojo.client.render.item.standdisc;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

@SuppressWarnings("deprecation")
public class StandDiscISTERModel implements BakedModel {
    private BakedModel existingModel;
    private ItemOverrides standOverrides;

    public StandDiscISTERModel(BakedModel existingModel) {
        this.existingModel = existingModel;
        this.standOverrides = new StandDiscOverrideList(existingModel.getOverrides());
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
        return standOverrides;
    }
}
