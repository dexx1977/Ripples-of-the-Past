package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import com.github.standobyte.jojo.entity.damaging.projectile.SCFlameSwingEntity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;

public class SCFlameRenderer extends FlameRenderer<SCFlameSwingEntity> {

    public SCFlameRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }
    
    @Override
    protected Vec3 getStartingPos(SCFlameSwingEntity entity) {
        return entity.getStartingPos();
    }
}
