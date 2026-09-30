package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HamonBubbleCutterModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class HamonBubbleCutterRenderer extends SimpleEntityRenderer<HamonBubbleCutterEntity, HamonBubbleCutterModel> {

    public HamonBubbleCutterRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new HamonBubbleCutterModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_bubble_cutter.png"));
    }

}
