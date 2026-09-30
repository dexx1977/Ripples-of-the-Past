package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.phys.Vec3;

public class MRFlameRenderer extends FlameRenderer<MRFlameEntity> {

    public MRFlameRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    protected Vec3 getStartingPos(MRFlameEntity entity) {
        return entity.getStartingPos();
    }
}
