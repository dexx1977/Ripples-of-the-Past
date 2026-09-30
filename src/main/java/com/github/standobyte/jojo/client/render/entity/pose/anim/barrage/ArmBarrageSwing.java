package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import java.util.Random;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Deprecated
public abstract class ArmBarrageSwing<T extends Entity, M extends EntityModel<T>> extends AdditionalBarrageSwing<T, M> {
    private static final Random RANDOM = new Random();
    private final HumanoidArm side;
    private final Vec3 offset;
    private final float zRot;
    
    public ArmBarrageSwing(IBarrageAnimation<T, M> barrageAnim, float ticks, float ticksMax, HumanoidArm side, double maxOffset) {
        super(barrageAnim, ticks, ticksMax);
        this.side = side;
        double upOffset = (RANDOM.nextDouble() - 0.5) * maxOffset;
        double leftOffset = RANDOM.nextDouble() * maxOffset / 2;
        double frontOffset = RANDOM.nextDouble() * 0.5;
        if (side == HumanoidArm.RIGHT) {
            leftOffset *= -1;
        }
        double atan = Mth.atan2(upOffset, leftOffset);
        zRot = maxOffset == 0 ? 0 : MathUtil.wrapRadians((float) (Math.PI / 2 - atan));
        offset = new Vec3(leftOffset, upOffset, frontOffset);
    }
    
    @Override
    public void poseAndRender(T entity, M model, PoseStack matrixStack, VertexConsumer buffer, 
            float yRotOffsetRad, float xRotRad, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        setArmOnlyModelVisibility(entity, model, side);
        float animRatio = ticks / ticksMax;
        double zAdditional = (0.5F - Math.abs(0.5F - animRatio));
        Vec3 offsetRot = new Vec3(offset.x, -offset.y, offset.z + zAdditional).xRot(xRotRad);
        matrixStack.pushPose();
        matrixStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
        barrageAnim.animateSwing(entity, model, animRatio, side, yRotOffsetRad, xRotRad, zRot);
        barrageAnim.beforeSwingAfterimageRender(matrixStack, model, animRatio, side);
        model.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.75F);
        matrixStack.popPose();
    }
    
    protected abstract void setArmOnlyModelVisibility(T entity, M model, HumanoidArm side);
}
