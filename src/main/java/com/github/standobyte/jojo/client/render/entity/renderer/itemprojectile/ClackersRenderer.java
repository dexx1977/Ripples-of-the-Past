package com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.ClackersModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class ClackersRenderer extends SimpleEntityRenderer<ClackersEntity, ClackersModel> {

    public ClackersRenderer(EntityRendererProvider.Context context) {
        super(context, new ClackersModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/clackers.png"));
    }

}
