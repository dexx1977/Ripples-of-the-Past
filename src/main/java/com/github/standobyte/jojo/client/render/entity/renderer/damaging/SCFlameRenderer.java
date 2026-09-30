package com.github.standobyte.jojo.client.render.entity.renderer.damaging;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.SCFlameSwingEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.phys.Vec3;

public class SCFlameRenderer extends FlameRenderer<SCFlameSwingEntity> {

    public SCFlameRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    protected Vec3 getStartingPos(SCFlameSwingEntity entity) {
        return entity.getStartingPos();
    }
}
