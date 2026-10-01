package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.function.Function;

import com.github.standobyte.jojo.action.stand.MagiciansRedFlameBurst;
import com.github.standobyte.jojo.action.stand.MagiciansRedRedBind;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import com.mojang.math.Axis;

// Made with Blockbench 3.9.2


public class MagiciansRedModel extends HumanoidStandModel<MagiciansRedEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    // The UVs of this model are laid out for a 128x128 texture (texOffs go up to 118),
    // so the declared size must be 128 - declaring 64 makes ModelPart normalize the
    // UVs by 64, sampling outside the texture and rendering the stand as a garbled mess.
    protected int texWidth = 128;
    protected int texHeight = 128;

    private ModelPart beakUpper;
    private ModelPart beakLower;
    private ModelPart feather;
    private ModelPart feather2;
    
    public MagiciansRedModel() {
        this(RenderType::entityTranslucent, 128, 128);
    }

    public MagiciansRedModel(Function<ResourceLocation, RenderType> renderType, int texWidth, int texHeight) {
        super(renderType, texWidth, texHeight);

        root = new ModelPart(this);
        root.setPos(0.0F, 24.0F, 0.0F);
        

        head = new ModelPart(this);
        head.setPos(0.0F, -24.0F, 0.0F);
        root.addChild(head);
        head.texOffs(0, 0).addBox(-3.5F, -7.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
        head.texOffs(0, 15).addBox(-4.1F, -7.4F, -4.4F, 1.0F, 7.0F, 2.0F, -0.4F, false);
        head.texOffs(0, 24).addBox(-4.1F, -1.6F, -4.4F, 1.0F, 2.0F, 2.0F, -0.4F, false);
        head.texOffs(20, 15).addBox(3.1F, -7.4F, -4.4F, 1.0F, 7.0F, 2.0F, -0.4F, true);
        head.texOffs(20, 24).addBox(3.1F, -1.6F, -4.4F, 1.0F, 2.0F, 2.0F, -0.4F, true);
        head.texOffs(80, 26).addBox(-2.5F, -1.0F, -4.5F, 5.0F, 1.0F, 2.0F, 0.25F, false);

        ModelPart headRight_r1;
        ModelPart headLeft_r1;
        
        headRight_r1 = new ModelPart(this);
        headRight_r1.setPos(0.1296F, 24.0F, -0.18F);
        ModelPart.addChild(head, headRight_r1);
        setRotationAngle(headRight_r1, 0.0F, 0.0478F, 0.0F);
        headRight_r1.texOffs(6, 15).addBox(-4.1F, -31.4F, -3.2F, 1.0F, 3.0F, 5.0F, -0.4F, false);
        headRight_r1.texOffs(6, 23).addBox(-4.1F, -28.0F, -3.2F, 1.0F, 2.0F, 5.0F, -0.4F, false);
        headRight_r1.texOffs(6, 30).addBox(-4.1F, -25.6F, -3.2F, 1.0F, 2.0F, 5.0F, -0.4F, false);

        headLeft_r1 = new ModelPart(this);
        headLeft_r1.setPos(-0.1296F, 24.0F, -0.18F);
        ModelPart.addChild(head, headLeft_r1);
        setRotationAngle(headLeft_r1, 0.0F, -0.0478F, 0.0F);
        headLeft_r1.texOffs(26, 15).addBox(3.1F, -31.4F, -3.2F, 1.0F, 3.0F, 5.0F, -0.4F, true);
        headLeft_r1.texOffs(26, 23).addBox(3.1F, -28.0F, -3.2F, 1.0F, 2.0F, 5.0F, -0.4F, true);
        headLeft_r1.texOffs(26, 30).addBox(3.1F, -25.6F, -3.2F, 1.0F, 2.0F, 5.0F, -0.4F, true);

        beakUpper = new ModelPart(this);
        beakUpper.setPos(0.0F, -1.0F, -4.0F);
        ModelPart.addChild(head, beakUpper);

        ModelPart beakUpper2;
        ModelPart beakUpperLeft;
        ModelPart beakUpperRight;
        ModelPart beakUpper3;
        ModelPart beakUpper4;
        ModelPart beakLowerLeft;
        ModelPart beakLowerRight;

        beakUpper2 = new ModelPart(this);
        beakUpper2.setPos(0.0F, 0.0F, 0.0F);
        beakUpper.addChild(beakUpper2);
        setRotationAngle(beakUpper2, 0.1745F, 0.0F, 0.0F);
        beakUpper2.texOffs(78, 7).addBox(-1.0F, -1.0F, -5.5F, 2.0F, 1.0F, 7.0F, 0.25F, false);
        beakUpper2.texOffs(66, 12).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.0F, 2.0F, 0.25F, false);

        beakUpperLeft = new ModelPart(this);
        beakUpperLeft.setPos(1.25F, -0.5F, -5.75F);
        beakUpper2.addChild(beakUpperLeft);
        setRotationAngle(beakUpperLeft, 0.0F, 0.2618F, 0.0F);
        beakUpperLeft.texOffs(89, 8).addBox(-1.25F, -0.5F, 0.25F, 1.0F, 1.0F, 7.0F, 0.25F, true);

        beakUpperRight = new ModelPart(this);
        beakUpperRight.setPos(-1.25F, -0.5F, -5.75F);
        beakUpper2.addChild(beakUpperRight);
        setRotationAngle(beakUpperRight, 0.0F, -0.2618F, 0.0F);
        beakUpperRight.texOffs(69, 8).addBox(0.25F, -0.5F, 0.25F, 1.0F, 1.0F, 7.0F, 0.25F, false);

        beakUpper3 = new ModelPart(this);
        beakUpper3.setPos(0.0F, -1.25F, -5.75F);
        beakUpper2.addChild(beakUpper3);
        setRotationAngle(beakUpper3, 0.7854F, 0.0F, 0.0F);
        beakUpper3.texOffs(87, 4).addBox(-0.5F, 0.25F, 0.25F, 1.0F, 1.0F, 1.0F, 0.25F, false);

        beakUpper4 = new ModelPart(this);
        beakUpper4.setPos(0.0F, 0.0F, 1.5F);
        beakUpper3.addChild(beakUpper4);
        setRotationAngle(beakUpper4, -0.7854F, 0.0F, 0.0F);
        beakUpper4.texOffs(79, 0).addBox(-0.5F, 0.25F, 0.25F, 1.0F, 1.0F, 6.0F, 0.25F, false);

        beakLower = new ModelPart(this);
        beakLower.setPos(0.0F, -0.75F, -4.75F);
        ModelPart.addChild(head, beakLower);
        beakLower.texOffs(79, 17).addBox(-1.0F, 0.0F, -4.75F, 2.0F, 1.0F, 6.0F, 0.0F, false);
        beakLower.texOffs(65, 20).addBox(-1.5F, 0.0F, -1.75F, 3.0F, 1.0F, 3.0F, 0.0F, false);
        beakLower.texOffs(72, 25).addBox(-1.0F, -0.3F, -3.75F, 2.0F, 1.0F, 4.0F, 0.0F, false);

        beakLowerLeft = new ModelPart(this);
        beakLowerLeft.setPos(1.0F, 0.5F, -4.75F);
        beakLower.addChild(beakLowerLeft);
        setRotationAngle(beakLowerLeft, 0.0F, 0.2618F, 0.0F);
        beakLowerLeft.texOffs(90, 19).addBox(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 0.0F, true);

        beakLowerRight = new ModelPart(this);
        beakLowerRight.setPos(-1.0F, 0.5F, -4.75F);
        beakLower.addChild(beakLowerRight);
        setRotationAngle(beakLowerRight, 0.0F, -0.2618F, 0.0F);
        beakLowerRight.texOffs(72, 19).addBox(0.0F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 0.0F, false);

        feather = new ModelPart(this);
        feather.setPos(0.0F, -6.5F, 4.5F);
        ModelPart.addChild(head, feather);
        setRotationAngle(feather, 0.3927F, 0.0F, 0.0F);
        feather.texOffs(30, 8).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 6.0F, 0.0F, false);
        feather.texOffs(40, 12).addBox(-1.0F, 0.5F, 4.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        feather2 = new ModelPart(this);
        feather2.setPos(0.0F, -1.0F, -2.5F);
        feather.addChild(feather2);
        setRotationAngle(feather2, 0.3491F, 0.0F, 0.0F);
        feather2.texOffs(30, 0).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 6.0F, 0.0F, false);
        feather2.texOffs(40, 4).addBox(-1.0F, 0.5F, 5.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        body = new ModelPart(this);
        body.setPos(0.0F, -24.0F, 0.0F);
        root.addChild(body);
        

        upperPart = new ModelPart(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        ModelPart.addChild(body, upperPart);
        

        torso = new ModelPart(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(0, 64).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        torso.texOffs(20, 64).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(24, 73).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);

        leftArm = convertLimb(new ModelPart(this));
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);
        leftArm.texOffs(32, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        leftArmJoint = new ModelPart(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(leftArm, leftArmJoint);
        leftArmJoint.texOffs(32, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftForeArm = new ModelPart(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(leftArm, leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightArm = convertLimb(new ModelPart(this));
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);
        rightArm.texOffs(0, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightArmJoint = new ModelPart(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(rightArm, rightArmJoint);
        rightArmJoint.texOffs(0, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightForeArm = new ModelPart(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(rightArm, rightForeArm);
        rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        leftLeg = convertLimb(new ModelPart(this));
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        ModelPart.addChild(body, leftLeg);
        leftLeg.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        leftLegJoint = new ModelPart(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(leftLeg, leftLegJoint);
        leftLegJoint.texOffs(96, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftLowerLeg = new ModelPart(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(leftLeg, leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightLeg = convertLimb(new ModelPart(this));
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        ModelPart.addChild(body, rightLeg);
        rightLeg.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightLegJoint = new ModelPart(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(rightLeg, rightLegJoint);
        rightLegJoint.texOffs(64, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightLowerLeg = new ModelPart(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(rightLeg, rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
    }

    @Override
    protected ModelPose<MagiciansRedEntity> initPoseReset() {
        return super.initPoseReset()
                .putRotation(RotationAngle.fromDegrees(beakUpper, 10F, 0.0F, 0.0F))
                .putRotation(new RotationAngle(beakLower, 0.0F, 0.0F, 0.0F));
    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    new RotationAngle(head, 0.0F, 0.1309F, 0.0F),
                    new RotationAngle(beakUpper, -0.3491F, 0.0F, 0.0F),
                    new RotationAngle(beakLower, 0.5236F, 0.0F, 0.0F),
                    new RotationAngle(body, 0.0F, -0.3927F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.0F, -2.3562F),
                    new RotationAngle(rightArm, 0.0F, 0.0F, 2.3562F),
                    new RotationAngle(leftLeg, 0.1745F, -0.7854F, 0.0F),
                    new RotationAngle(rightLeg, -1.5708F, -0.7854F, 0.0F),
                    new RotationAngle(rightLowerLeg, 2.3562F, 0.0F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(head, -0.7854F, 0.0F, 0.0F),
                    new RotationAngle(beakUpper, -0.3491F, 0.0F, 0.0F),
                    new RotationAngle(beakLower, 0.5236F, 0.0F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 2.3562F, -2.8798F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 2.3562F),
                    new RotationAngle(rightArm, 0.0F, -2.3562F, 2.8798F),
                    new RotationAngle(rightForeArm, 0.0F, 0.0F, -2.3562F),
                    new RotationAngle(leftLeg, 0.2182F, 0.0F, 0.1309F),
                    new RotationAngle(rightLeg, 0.5236F, 0.0F, -0.1309F)
            }
        };
    }

    @Override
    public void setupAnim(MagiciansRedEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
    }
    
    @Override
    protected void initActionPoses() {
        actionAnim.put(MagiciansRedFlameBurst.FLAME_BURST_POSE, new PosedActionAnimation.Builder<MagiciansRedEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<MagiciansRedEntity>(new RotationAngle[] {
                        RotationAngle.fromDegrees(beakUpper, -20F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(beakLower, 30F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(leftArm, -90F, 60F, 12.5F),
                        new RotationAngle(leftForeArm, -0.2618F, 0.0F, 0.5236F),
                        new RotationAngle(rightArm, -1.3963F, -0.9163F, -0.1745F),
                        new RotationAngle(rightForeArm, -0.5236F, 0.0F, -0.6981F),
                        new RotationAngle(leftLeg, -0.3927F, 0.0F, 0.0873F),
                        new RotationAngle(leftLowerLeg, 0.7854F, 0.0F, 0.0F),
                        new RotationAngle(rightLeg, 0.2618F, 0.0F, -0.0873F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
                    leftArm.setPos(4.0F, -10.0F, 0.0F);
                    rightArm.setPos(-4.0F, -10.0F, 0.0F);
                    setRotationAngle(head, xRotRad - 0.3927F, 0.0F, 0.0F);
                }).createRigid())
                .build(idlePose));

        ModelPose<MagiciansRedEntity> kickPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -15F, 0F, 0F), 
                RotationAngle.fromDegrees(body, -49.1066F, -20.7048F, 22.2077F),
                RotationAngle.fromDegrees(leftLeg, 45F, 30F, 0F),
                RotationAngle.fromDegrees(rightLeg, -75F, 30F, 30F),
                RotationAngle.fromDegrees(rightLowerLeg, 60F, 0F, 0F)
        });
        ModelPose<MagiciansRedEntity> kickPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -30F, -15F, 0F), 
                RotationAngle.fromDegrees(body, -54.7356F, -30F, 35.2644F),
                new RotationAngle(beakUpper, -0.3491F, 0.0F, 0.0F),
                new RotationAngle(beakLower, 0.5236F, 0.0F, 0.0F),
                RotationAngle.fromDegrees(leftArm, -60F, 0.0F, -45F),
                RotationAngle.fromDegrees(leftForeArm, 0.0F, 0.0F, 50F),
                RotationAngle.fromDegrees(rightArm, 45F, -10F, 10F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, 0F),
                RotationAngle.fromDegrees(leftLeg, 45F, 45F, 0F),
                RotationAngle.fromDegrees(rightLeg, -105F, 30F, 30F),
                RotationAngle.fromDegrees(rightLowerLeg, 90F, 0F, 0F)
        });
        ModelPose<MagiciansRedEntity> kickPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -45F, -10F, 0F), 
                RotationAngle.fromDegrees(body, -59.3179F, -27.034F, 37.4537F),
                RotationAngle.fromDegrees(leftArm, -135F, -15F, 30F),
                RotationAngle.fromDegrees(rightArm, -180F, 30F, -45F).noDegreesWrapping(),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -45F),
                RotationAngle.fromDegrees(leftLeg, 50F, 45F, 0F),
                RotationAngle.fromDegrees(rightLeg, -42.6168F, 9.6867F, 44.9391F),
                RotationAngle.fromDegrees(rightLowerLeg, 0F, 0F, 0F)
        });
        ModelPose<MagiciansRedEntity> kickRecoveryArmHackyFix = ((ModelPose<MagiciansRedEntity>) idlePose).copy()
                .putRotation(new RotationAngle(rightArm, 0.1309F, 0.0F, 0.4363F).noDegreesWrapping());
        actionAnim.put(StandPose.HEAVY_ATTACK_FINISHER, new PosedActionAnimation.Builder<MagiciansRedEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(kickPose1, kickPose2))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransition<>(kickPose2, kickPose3))
                .build(kickRecoveryArmHackyFix));
        
        actionAnim.put(MagiciansRedRedBind.RED_BIND_POSE, new PosedActionAnimation.Builder<MagiciansRedEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<>(new RotationAngle[] {
                        new RotationAngle(leftArm, -1.4708F, 0.4712F, 0.0F),
                        new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rightArm, -1.6708F, -0.4712F, 0.0F),
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F),
                }))
                .build(idlePose));
        
        super.initActionPoses();
    }

    @Override
    protected ModelPose<MagiciansRedEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(beakUpper, 0.0F, 0.0F, 0.0F),
                new RotationAngle(beakLower, 0.0F, 0.0F, 0.0F),
                new RotationAngle(body, 0.0F, 0.1309F, 0.0F),
                new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                new RotationAngle(leftArm, 0.3054F, 0.0F, -0.2182F),
                new RotationAngle(leftForeArm, -2.0944F, -0.2618F, 1.0472F),
                new RotationAngle(rightArm, 0.1309F, 0.0F, 0.4363F),
                new RotationAngle(rightForeArm, -2.3562F, 0.2618F, -1.8326F),
                new RotationAngle(leftLeg, 0.1309F, -0.1309F, 0.0F),
                new RotationAngle(leftLowerLeg, 0.2618F, 0.0F, 0.0F),
                new RotationAngle(rightLeg, 0.0F, -0.1309F, 0.2182F),
                new RotationAngle(rightLowerLeg, 0.1309F, 0.0F, 0.0F),
        });
    }

    @Override
    protected ModelPose<MagiciansRedEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(leftArm, 0.3927F, 0.0F, -0.1745F),
                new RotationAngle(leftForeArm, -1.9635F, -0.1309F, 1.0472F),
                new RotationAngle(rightArm, 0.1309F, 0.0F, 0.3054F),
                new RotationAngle(rightForeArm, -2.3562F, 0.2618F, -1.7017F)
        });
    }
    
    @Override
    public void setupFirstPersonRotations(PoseStack matrixStack, MagiciansRedEntity entity, float xRot, float yRot, float yBodyRot) {
        if (standPose == MagiciansRedFlameBurst.FLAME_BURST_POSE) {
            float xRotRad = xRot * MathUtil.DEG_TO_RAD;
            matrixStack.mulPose(Axis.XP.rotation(xRotRad));
            
            matrixStack.translate(0, -entity.getBbHeight() * 0.25, 0.25);
            matrixStack.mulPose(Axis.XP.rotation(head.xRot - xRotRad));
            matrixStack.translate(0, entity.getBbHeight() * 0.25, -0.25);
            
            matrixStack.mulPose(Axis.YP.rotationDegrees(180 + yBodyRot));
            matrixStack.translate(0, -entity.getEyeHeight(), 0);
        }
        else {
            super.setupFirstPersonRotations(matrixStack, entity, xRot, yRot, yBodyRot);
        }
    }
}
