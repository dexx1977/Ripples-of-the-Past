package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.HierophantGreenModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class HierophantGreenRenderer extends StandEntityRenderer<HierophantGreenEntity, StandEntityModel<HierophantGreenEntity>> {

    public HierophantGreenRenderer(EntityRendererProvider.Context context) {
        super(context, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "hierophant_green"), HierophantGreenModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/hierophant_green.png"), 0);
    }
}
