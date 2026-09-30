package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.CrimsonBubbleModel;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class CrimsonBubbleRenderer extends SimpleEntityRenderer<CrimsonBubbleEntity, CrimsonBubbleModel> {

    public CrimsonBubbleRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new CrimsonBubbleModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/crimson_bubble.png"));
    }
    
}
