package com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.BladeHatEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class BladeHatRenderer extends SimpleEntityRenderer<BladeHatEntity, BladeHatEntityModel> {

    public BladeHatRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new BladeHatEntityModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/opened_blade_hat.png"));
    }

}
