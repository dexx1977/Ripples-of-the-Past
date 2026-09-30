package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.CrazyDiamondModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.entity.stand.stands.CrazyDiamondEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class CrazyDiamondRenderer extends StandEntityRenderer<CrazyDiamondEntity, StandEntityModel<CrazyDiamondEntity>> {

    public CrazyDiamondRenderer(EntityRendererProvider.Context context) {
        super(context, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "crazy_diamond"), CrazyDiamondModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/crazy_diamond.png"), 0);
    }
}
