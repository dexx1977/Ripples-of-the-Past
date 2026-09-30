package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.SnakeMufflerModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SnakeMufflerEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class SnakeMufflerRenderer extends ExtendingEntityRenderer<SnakeMufflerEntity, SnakeMufflerModel> {

    public SnakeMufflerRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new SnakeMufflerModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/satiporoja_scarf.png"));
    }
}
