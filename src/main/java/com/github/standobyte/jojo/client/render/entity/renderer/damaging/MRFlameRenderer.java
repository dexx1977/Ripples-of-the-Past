package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;

public class MRFlameRenderer extends FlameRenderer<MRFlameEntity> {

    public MRFlameRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }
    
    @Override
    protected Vec3 getStartingPos(MRFlameEntity entity) {
        return entity.getStartingPos();
    }
}
