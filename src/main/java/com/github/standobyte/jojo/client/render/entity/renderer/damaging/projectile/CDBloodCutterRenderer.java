package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.CDBloodCutterModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class CDBloodCutterRenderer extends SimpleEntityRenderer<CDBloodCutterEntity, CDBloodCutterModel> {

    public CDBloodCutterRenderer(EntityRendererProvider.Context context) {
        super(context, new CDBloodCutterModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/cd_blood_cutter.png"));
    }

}
