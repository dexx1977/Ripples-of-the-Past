package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.PillarmanRibModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanRibEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class PillarmanRibRenderer extends ExtendingEntityRenderer<PillarmanRibEntity, PillarmanRibModel> {

    public PillarmanRibRenderer(EntityRendererProvider.Context context) {
        super(context, new PillarmanRibModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/pillarman_ribs.png"));
    }

}
