package com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.BladeHatEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class BladeHatRenderer extends SimpleEntityRenderer<BladeHatEntity, BladeHatEntityModel> {

    public BladeHatRenderer(EntityRendererProvider.Context context) {
        super(context, new BladeHatEntityModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/opened_blade_hat.png"));
    }

}
