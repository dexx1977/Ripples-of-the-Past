package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.HGStringModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class HGBarrierRenderer extends HGStringAbstractRenderer<HGBarrierEntity> {

    public HGBarrierRenderer(EntityRendererProvider.Context context) {
        super(context, new HGStringModel<HGBarrierEntity>());
    }
}
