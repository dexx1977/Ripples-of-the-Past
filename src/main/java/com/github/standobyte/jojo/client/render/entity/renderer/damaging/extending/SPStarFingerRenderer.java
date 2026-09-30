package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.SPStarFingerModel;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class SPStarFingerRenderer extends ExtendingEntityRenderer<SPStarFingerEntity, SPStarFingerModel> {

    public SPStarFingerRenderer(EntityRendererProvider.Context context) {
        super(context, new SPStarFingerModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/sp_star_finger.png"));
    }
    
    @Override
    public ResourceLocation getTextureLocation(SPStarFingerEntity entity) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(entity.getStandSkin()), texPath);
    }


}
