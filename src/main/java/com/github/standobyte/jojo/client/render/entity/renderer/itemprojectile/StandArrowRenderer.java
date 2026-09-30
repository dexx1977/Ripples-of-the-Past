package com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class StandArrowRenderer extends ArrowRenderer<StandArrowEntity> {

    private static final ResourceLocation STAND_ARROW_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/stand_arrow.png");

    public StandArrowRenderer(EntityRenderDispatcher rendererManager) {
        super(rendererManager);
    }

    @Override
    public ResourceLocation getTextureLocation(StandArrowEntity entity) {
        return STAND_ARROW_LOCATION;
    }
}
