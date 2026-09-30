package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.MagiciansRedModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.MagiciansRedFlameLayer;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

public class MagiciansRedRenderer extends StandEntityRenderer<MagiciansRedEntity, StandEntityModel<MagiciansRedEntity>> {
    public static final Material MR_FIRE_0 = new Material(
            InventoryMenu.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "entity/stand/magicians_red_fire_0"));
    public static final Material MR_FIRE_1 = new Material(
            InventoryMenu.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "entity/stand/magicians_red_fire_1"));
    public static final Supplier<TextureAtlasSprite> FIRE_0_SPRITE = MR_FIRE_0::sprite;
    public static final Supplier<TextureAtlasSprite> FIRE_1_SPRITE = MR_FIRE_1::sprite;

    public MagiciansRedRenderer(EntityRendererProvider.Context context) {
        super(context, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "magicians_red"), MagiciansRedModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/magicians_red.png"), 0);
        addLayer(new MagiciansRedFlameLayer(this));
    }
}
