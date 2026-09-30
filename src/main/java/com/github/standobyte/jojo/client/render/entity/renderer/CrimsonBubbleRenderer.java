package com.github.standobyte.jojo.client.render.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.CrimsonBubbleModel;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class CrimsonBubbleRenderer extends SimpleEntityRenderer<CrimsonBubbleEntity, CrimsonBubbleModel> {

    public CrimsonBubbleRenderer(EntityRendererProvider.Context context) {
        super(context, new CrimsonBubbleModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/crimson_bubble.png"));
    }
    
}
