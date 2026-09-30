package com.github.standobyte.jojo.client.render.entity.pose.anim;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.function.UnaryOperator;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;

public class CopyBipedUserPose<T extends StandEntity> implements IModelPose<T> {
    private final HumanoidStandModel<T> model;
    
    public CopyBipedUserPose(HumanoidStandModel<T> model) {
        this.model = model;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad,
            HumanoidArm side) {
        LivingEntity user = entity.getUser();
        if (user != null) {
            EntityRenderer<?> userRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(user);
            if (userRenderer instanceof LivingEntityRenderer) {
                EntityModel<?> userModel = ((LivingEntityRenderer<?, ?>) userRenderer).getModel();
                if (userModel instanceof HumanoidModel) {
                    HumanoidModel<?> userBipedModel = (HumanoidModel<?>) userModel;

                    model.resetPose(entity);

                    copyRotation(model.getHead(), userBipedModel.head);
                    copyRotation(model.getTorso(), userBipedModel.body);
                    copyRotation(model.getArm(HumanoidArm.LEFT), userBipedModel.leftArm);
                    copyRotation(model.getArm(HumanoidArm.RIGHT), userBipedModel.rightArm);
                    copyRotation(model.getLeg(HumanoidArm.LEFT), userBipedModel.leftLeg);
                    copyRotation(model.getLeg(HumanoidArm.RIGHT), userBipedModel.rightLeg);
                }
            }
        }        
    }
    
    private void copyRotation(ModelPart to, ModelPart from) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
    }

    @Override
    public IModelPose<T> setEasing(UnaryOperator<Float> function) {
        return this;
    }

}
