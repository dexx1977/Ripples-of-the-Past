package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public abstract class RepeatingModel<T extends Entity> extends EntityModel<T> {
    private static final Random RANDOM = new Random();
    
    private float length;
    private float yRotation;
    private float xRotation;

    protected RepeatingModel() {
        super(RenderType::entityTranslucent);
    }

    @Nullable
    protected abstract ModelPart getMainPart();
    
    protected abstract float getMainPartLength();
    
    protected abstract ModelPart getRepeatingPart();
    
    protected abstract float getRepeatingPartLength();
    
    protected boolean squareModelRandomRotation() {
        return false;
    }
    
    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        this.yRotation = yRotationOffset;
        this.xRotation = xRotation;
        RANDOM.setSeed(entity.getId());
    }
    
    public void setLength(float length) {
        this.length = length;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        matrixStack.mulPose(Axis.XP.rotationDegrees(xRotation));
        float modelLength = length;
        ModelPart mainPart = getMainPart();
        float mainPartLength = getMainPartLength() / 16F;
        if (mainPart != null && modelLength >= mainPartLength) {
            mainPart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            modelLength -= mainPartLength;
        }
        ModelPart repeatingPart = getRepeatingPart();
        float repeatingLength = getRepeatingPartLength() / 16F;
        while (modelLength >= repeatingLength) {
            if (squareModelRandomRotation()) {
                repeatingPart.zRot = RANDOM.nextInt(4) * 90F * MathUtil.DEG_TO_RAD;
            }
            repeatingPart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            modelLength -= repeatingLength;
            matrixStack.translate(0, 0, repeatingLength);
        }
        repeatingPart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        
        matrixStack.mulPose(Axis.XP.rotationDegrees(-xRotation));
        matrixStack.mulPose(Axis.YP.rotationDegrees(-yRotation));
        matrixStack.popPose();
    }
}
