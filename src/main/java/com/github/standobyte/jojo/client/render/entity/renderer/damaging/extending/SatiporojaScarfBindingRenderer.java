package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.SatiporojaScarfBindingModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfBindingEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class SatiporojaScarfBindingRenderer extends ExtendingEntityRenderer<SatiporojaScarfBindingEntity, SatiporojaScarfBindingModel> {

    public SatiporojaScarfBindingRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new SatiporojaScarfBindingModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/satiporoja_scarf.png"));
    }
}
