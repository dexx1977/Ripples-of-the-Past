package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;

@Deprecated
public class BarrageSwingsHolder<T extends Entity, M extends EntityModel<T>> {
    private List<AdditionalBarrageSwing<T, M>> barrageSwings = new LinkedList<>();
    private float loopLast = -1;

    public void addSwing(AdditionalBarrageSwing<T, M> swing) {
        barrageSwings.add(swing);
    }
    
    public void updateSwings(Minecraft mc) {
        if (!mc.isPaused() && !barrageSwings.isEmpty()) {
            float timeDelta = mc.getDeltaFrameTime();
            Iterator<AdditionalBarrageSwing<T, M>> iter = barrageSwings.iterator();
            while (iter.hasNext()) {
                AdditionalBarrageSwing<T, M> swing = iter.next();
                swing.addDelta(timeDelta);
                if (swing.removeSwing()) {
                    iter.remove();
                }
            }
        }
    }
    
    public boolean hasSwings() {
        return !barrageSwings.isEmpty();
    }

    public void renderBarrageSwings(M model, T entity, PoseStack matrixStack, VertexConsumer buffer, 
            int packedLight, int packedOverlay, float yRotRad, float xRotRad, float red, float green, float blue, float alpha) {
        for (AdditionalBarrageSwing<T, M> swing : barrageSwings) {
            swing.poseAndRender(entity, model, matrixStack, buffer, yRotRad, xRotRad, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
    
    public void setLoopCount(float loopCount) {
        this.loopLast = loopCount;
    }
    
    public float getLoopCount() {
        return loopLast;
    }
    
    public void resetSwingTime() {
        loopLast = -1;
    }
}
