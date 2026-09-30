package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HGEmeraldModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class HGEmeraldRenderer extends SimpleEntityRenderer<HGEmeraldEntity, HGEmeraldModel> {

    public HGEmeraldRenderer(EntityRendererProvider.Context context) {
        super(context, new HGEmeraldModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hg_emerald.png"));
    }
    
    @Override
    public ResourceLocation getTextureLocation(HGEmeraldEntity entity) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(entity.getStandSkin()), texPath);
    }

}
