package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.block.WoodenCoffinBlock;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidModel.class)
public abstract class BipedModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart body;
    
    @Inject(method = "setupAnim", at = @At("TAIL"))
    public void jojoBipedModelPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null && vehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
            leftArm.xRot = (float)Math.PI;
            leftArm.yRot = 0;
            leftArm.zRot = 0;
            rightArm.xRot = (float)Math.PI;
            rightArm.yRot = 0;
            rightArm.zRot = 0;
            body.zRot = 0;
        }
        
        if (entity.isSleeping() && WoodenCoffinBlock.isSleepingInCoffin(entity)) {
            leftArm.xRot = 0;
            leftArm.zRot = 0;
            rightArm.xRot = 0;
            rightArm.zRot = 0;
        }
        
        for (Entity passenger : entity.getPassengers()) {
            if (CocoJumboTurtleEntity.isCarriedTurtle(passenger, entity)) {
                switch (entity.getMainArm()) {
                case LEFT:
                    rightArm.xRot = -(float)Math.PI / 10f;
                    rightArm.zRot = 0;
                    break;
                case RIGHT:
                    leftArm.xRot = -(float)Math.PI / 10f;
                    leftArm.zRot = 0;
                    break;
                }
                break;
            }
        }
    }
    
}
