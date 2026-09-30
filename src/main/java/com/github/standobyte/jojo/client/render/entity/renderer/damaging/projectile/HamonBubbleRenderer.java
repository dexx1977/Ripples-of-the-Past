package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HamonBubbleModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class HamonBubbleRenderer extends SimpleEntityRenderer<HamonBubbleEntity, HamonBubbleModel> {

    public HamonBubbleRenderer(EntityRendererProvider.Context context) {
        super(context, new HamonBubbleModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_bubble.png"));
    }

}
