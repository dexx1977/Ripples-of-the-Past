package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.model.projectile.CDBlockBulletModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CDBlockBulletRenderer extends SimpleEntityRenderer<CDBlockBulletEntity, CDBlockBulletModel> {

    public CDBlockBulletRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new CDBlockBulletModel(), null);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CDBlockBulletEntity entity) {
        ResourceLocation texture = entity.getBlockTex();
        if (texture == null) {
            texture = getBlockTexture(entity);
            entity.setBlockTex(texture);
        }
        return texture;
    }
    
    private static final Random RANDOM = new Random();
    private static final ResourceLocation GLASS_TEXTURE = new ResourceLocation("textures/block/glass.png");
    private ResourceLocation getBlockTexture(CDBlockBulletEntity entity) {
        if (entity.getBlock() != null) {
            ResourceLocation texture = getBlockTexture(entity.getBlock().defaultBlockState());
            return texture != null ? texture : GLASS_TEXTURE;
        }
        return GLASS_TEXTURE;
    }
    
    @Nullable
    public static ResourceLocation getBlockTexture(BlockState blockState) {
        BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> quads = blockModel.getQuads(blockState, Direction.NORTH, RANDOM, EmptyModelData.INSTANCE);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            return getSpriteTexture(sprite).orElse(null);
        }
        return null;
    }
    
    public static Optional<ResourceLocation> getSpriteTexture(TextureAtlasSprite sprite) {
        if (sprite != null) {
            ResourceLocation name = sprite.getName();
            if (name != null) {
                return Optional.of(new ResourceLocation(name.getNamespace(), "textures/" + name.getPath() + ".png"));
            }
        }
        return Optional.empty();
    }
}
