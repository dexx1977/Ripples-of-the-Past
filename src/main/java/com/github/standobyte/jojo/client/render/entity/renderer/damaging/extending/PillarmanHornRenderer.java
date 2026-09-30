package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.PillarmanHornModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanHornEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class PillarmanHornRenderer extends ExtendingEntityRenderer<PillarmanHornEntity, PillarmanHornModel> {

    public PillarmanHornRenderer(EntityRendererProvider.Context context) {
        super(context, new PillarmanHornModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/pm_horn.png"));
    }

}
