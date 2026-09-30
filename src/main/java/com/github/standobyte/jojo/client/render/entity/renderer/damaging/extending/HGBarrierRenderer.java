package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.HGStringModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class HGBarrierRenderer extends HGStringAbstractRenderer<HGBarrierEntity> {

    public HGBarrierRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new HGStringModel<HGBarrierEntity>());
    }
}
