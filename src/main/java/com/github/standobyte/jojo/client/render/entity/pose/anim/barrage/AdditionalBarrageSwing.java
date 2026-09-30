package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;

@Deprecated
public abstract class AdditionalBarrageSwing<T extends Entity, M extends EntityModel<T>> {
    protected final IBarrageAnimation<T, M> barrageAnim;
    protected float ticks;
    protected final float ticksMax;
    
    public AdditionalBarrageSwing(IBarrageAnimation<T, M> barrageAnim, float startingAnim, float animMax) {
        this.barrageAnim = barrageAnim;
        this.ticks = startingAnim;
        this.ticksMax = animMax;
    }
    
    public void addDelta(float delta) {
        ticks += delta * 0.75F;
    }
    
    public boolean removeSwing() {
        return ticks >= ticksMax * 0.75F;
    }
    
    
    
    public abstract void poseAndRender(T entity, M model, PoseStack matrixStack, VertexConsumer buffer, 
            float yRotOffsetRad, float xRotRad, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
}
