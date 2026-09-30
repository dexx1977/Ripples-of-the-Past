package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.HGStringModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class HGStringRenderer extends HGStringAbstractRenderer<HGStringEntity> {

    public HGStringRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new HGStringModel<HGStringEntity>());
    }
}
