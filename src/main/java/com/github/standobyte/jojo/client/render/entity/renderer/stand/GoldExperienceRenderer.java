package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.GoldExperienceModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class GoldExperienceRenderer extends StandEntityRenderer<GoldExperienceEntity, GoldExperienceModel> {

    public GoldExperienceRenderer(EntityRendererProvider.Context context) {
        super(context, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "gold_experience"), GoldExperienceModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/gold_experience.png"), 0);
    }
}
