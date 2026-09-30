package com.github.standobyte.jojo.client.render.entity.animnew;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;

public class LivingEntityRenderState {
    public float yRot;
    public float xRot;
    
    public static void extract(LivingEntity entity, float yRot, float xRot) {
        reusedState.yRot = Mth.wrapDegrees(yRot);
        reusedState.xRot = xRot;
    }
    
    public static void clear() {
        reusedState.yRot = 0;
        reusedState.xRot = 0;
    }
    
    public static final LivingEntityRenderState reusedState = new LivingEntityRenderState();
}
