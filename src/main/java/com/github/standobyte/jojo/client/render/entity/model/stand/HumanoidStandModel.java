package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.particle.custom.StandCrumbleParticle;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseSided;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.StandTwoHandedBarrageAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.TargetHitPart;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Made with Blockbench 3.9.2


public class HumanoidStandModel<T extends StandEntity> extends StandEntityModel<T> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    public ModelPart head;
    public ModelPart headRot;
    public ModelPart body;
    public ModelPart upperPart;
    public ModelPart torso;
    public ModelPart leftArmXRot;
    public XRotationModelRenderer leftArm;
    public ModelPart leftArmJoint;
    public ModelPart leftForeArm;
    public ModelPart rightArmXRot;
    public XRotationModelRenderer rightArm;
    public ModelPart rightArmJoint;
    public ModelPart rightForeArm;
    public ModelPart leftLegXRot;
    public XRotationModelRenderer leftLeg;
    public ModelPart leftLegJoint;
    public ModelPart leftLowerLeg;
    public ModelPart rightLegXRot;
    public XRotationModelRenderer rightLeg;
    public ModelPart rightLegJoint;
    public ModelPart rightLowerLeg;
    

    public HumanoidStandModel() {
        this(128, 128);
    }
    
    public HumanoidStandModel(int textureWidth, int textureHeight) {
        this(RenderType::entityTranslucent, textureWidth, textureHeight);
    }
    
    public static <T extends StandEntity> HumanoidStandModel<T> createBasic() {
        HumanoidStandModel<T> model = new HumanoidStandModel<>();

        model.head = new ModelPart(model);
        model.head.setPos(0.0F, 0.0F, 0.0F);

        model.body = new ModelPart(model);
        model.body.setPos(0.0F, 0.0F, 0.0F);

        model.upperPart = new ModelPart(model);
        model.upperPart.setPos(0.0F, 12.0F, 0.0F);
        ModelPart.addChild(model.body, model.upperPart);

        model.torso = new ModelPart(model);
        model.torso.setPos(0.0F, -12.0F, 0.0F);
        model.upperPart.addChild(model.torso);

        model.leftArmXRot = new ModelPart(model);
        model.leftArmXRot.setPos(6.0F, -10.0F, 0.0F);
        model.upperPart.addChild(model.leftArmXRot);

        model.leftArm = new XRotationModelRenderer(model);
        model.leftArm.setPos(0.0F, 0.0F, 0.0F);
        model.leftArmXRot.addChild(model.leftArm);

        model.leftArmJoint = new ModelPart(model);
        model.leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(model.leftArm, model.leftArmJoint);

        model.leftForeArm = new ModelPart(model);
        model.leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(model.leftArm, model.leftForeArm);

        model.rightArmXRot = new ModelPart(model);
        model.rightArmXRot.setPos(-6.0F, -10.0F, 0.0F);
        model.upperPart.addChild(model.rightArmXRot);

        model.rightArm = new XRotationModelRenderer(model);
        model.rightArm.setPos(0.0F, 0.0F, 0.0F);
        model.rightArmXRot.addChild(model.rightArm);

        model.rightArmJoint = new ModelPart(model);
        model.rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(model.rightArm, model.rightArmJoint);

        model.rightForeArm = new ModelPart(model);
        model.rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(model.rightArm, model.rightForeArm);

        model.leftLegXRot = new ModelPart(model);
        model.leftLegXRot.setPos(2.0F, 12.0F, 0.0F);
        ModelPart.addChild(model.body, model.leftLegXRot);

        model.leftLeg = new XRotationModelRenderer(model);
        model.leftLeg.setPos(0.0F, 0.0F, 0.0F);
        model.leftLegXRot.addChild(model.leftLeg);

        model.leftLegJoint = new ModelPart(model);
        model.leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(model.leftLeg, model.leftLegJoint);

        model.leftLowerLeg = new ModelPart(model);
        model.leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(model.leftLeg, model.leftLowerLeg);

        model.rightLegXRot = new ModelPart(model);
        model.rightLegXRot.setPos(-2.0F, 12.0F, 0.0F);
        ModelPart.addChild(model.body, model.rightLegXRot);

        model.rightLeg = new XRotationModelRenderer(model);
        model.rightLeg.setPos(0.0F, 0.0F, 0.0F);
        model.rightLegXRot.addChild(model.rightLeg);

        model.rightLegJoint = new ModelPart(model);
        model.rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(model.rightLeg, model.rightLegJoint);

        model.rightLowerLeg = new ModelPart(model);
        model.rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(model.rightLeg, model.rightLowerLeg);

        model.head          .texOffs(0, 0)    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        model.torso         .texOffs(0, 64)   .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        model.leftArm       .texOffs(32, 108) .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        model.leftArmJoint  .texOffs(32, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true);
        model.leftForeArm   .texOffs(32, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        model.rightArm      .texOffs(0, 108)  .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        model.rightArmJoint .texOffs(0, 102)  .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false);
        model.rightForeArm  .texOffs(0, 118)  .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        model.leftLeg       .texOffs(96, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        model.leftLegJoint  .texOffs(96, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true);
        model.leftLowerLeg  .texOffs(96, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        model.rightLeg      .texOffs(64, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        model.rightLegJoint .texOffs(64, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false);
        model.rightLowerLeg .texOffs(64, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        return model;
    }
    
    public HumanoidStandModel(Function<ResourceLocation, RenderType> renderType, int textureWidth, int textureHeight) {
        super(renderType, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);


        upperPart = new ModelPart(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        ModelPart.addChild(body, upperPart);


        torso = new ModelPart(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);

        leftArm = convertLimb(new ModelPart(this));
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);

        leftArmJoint = new ModelPart(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(leftArm, leftArmJoint);

        leftForeArm = new ModelPart(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(leftArm, leftForeArm);

        rightArm = convertLimb(new ModelPart(this));
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);

        rightArmJoint = new ModelPart(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(rightArm, rightArmJoint);

        rightForeArm = new ModelPart(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        ModelPart.addChild(rightArm, rightForeArm);

        leftLeg = convertLimb(new ModelPart(this));
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        ModelPart.addChild(body, leftLeg);

        leftLegJoint = new ModelPart(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(leftLeg, leftLegJoint);

        leftLowerLeg = new ModelPart(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(leftLeg, leftLowerLeg);

        rightLeg = convertLimb(new ModelPart(this));
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        ModelPart.addChild(body, rightLeg);

        rightLegJoint = new ModelPart(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(rightLeg, rightLegJoint);

        rightLowerLeg = new ModelPart(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        ModelPart.addChild(rightLeg, rightLowerLeg);
        
        
        baseHumanoidBoxGenerators = ImmutableMap.<Supplier<ModelPart>, Consumer<ModelPart>>builder()
                .put(() -> head, part ->          part.texOffs(0, 0)    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false))
                .put(() -> torso, part ->         part.texOffs(0, 64)   .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false))
                .put(() -> leftArm, part ->       part.texOffs(32, 108) .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> leftArmJoint, part ->  part.texOffs(32, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true))
                .put(() -> leftForeArm, part ->   part.texOffs(32, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(() -> rightArm, part ->      part.texOffs(0, 108)  .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> rightArmJoint, part -> part.texOffs(0, 102)  .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false))
                .put(() -> rightForeArm, part ->  part.texOffs(0, 118)  .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(() -> leftLeg, part ->       part.texOffs(96, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> leftLegJoint, part ->  part.texOffs(96, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true))
                .put(() -> leftLowerLeg, part ->  part.texOffs(96, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(() -> rightLeg, part ->      part.texOffs(64, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> rightLegJoint, part -> part.texOffs(64, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false))
                .put(() -> rightLowerLeg, part -> part.texOffs(64, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .build();
    }
    
    @Deprecated
    protected final XRotationModelRenderer convertLimb(ModelPart limbModelPart) {
        return new XRotationModelRenderer(this);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        putNamedModelPart("head", head);
        putNamedModelPart("headRot", headRot);
        putNamedModelPart("body", body);
        putNamedModelPart("upperPart", upperPart);
        putNamedModelPart("torso", torso);
        putNamedModelPart("leftArm", leftArm);
        putNamedModelPart("leftArmXRot", leftArmXRot);
        putNamedModelPart("leftArm", leftArm);
        putNamedModelPart("leftForeArm", leftForeArm);
        putNamedModelPart("rightArm", rightArm);
        putNamedModelPart("rightArmXRot", rightArmXRot);
        putNamedModelPart("rightArm", rightArm);
        putNamedModelPart("rightForeArm", rightForeArm);
        putNamedModelPart("leftLeg", leftLeg);
        putNamedModelPart("leftLegXRot", leftLegXRot);
        putNamedModelPart("leftLeg", leftLeg);
        putNamedModelPart("leftLowerLeg", leftLowerLeg);
        putNamedModelPart("rightLeg", rightLeg);
        putNamedModelPart("rightLegXRot", rightLegXRot);
        putNamedModelPart("rightLeg", rightLeg);
        putNamedModelPart("rightLowerLeg", rightLowerLeg);
    }

    @Deprecated private final Map<Supplier<ModelPart>, Consumer<ModelPart>> baseHumanoidBoxGenerators;
    @Deprecated
    protected final void addHumanoidBaseBoxes(@Nullable Predicate<ModelPart> partPredicate) {
        for (Map.Entry<Supplier<ModelPart>, Consumer<ModelPart>> entry : baseHumanoidBoxGenerators.entrySet()) {
            ModelPart modelRenderer = entry.getKey().get();
            if (partPredicate == null || partPredicate.test(modelRenderer)) {
                entry.getValue().accept(modelRenderer);
            }
        }
    }

    @Override
    public void updatePartsVisibility(VisibilityMode mode) {
        VisibilityMode baseMode = mode.baseMode;
        boolean setVisible = !mode.isInverted;
        
        ModelPart leftArm = getArm(HumanoidArm.LEFT);
        ModelPart rightArm = getArm(HumanoidArm.RIGHT);
        ModelPart leftLeg = getLeg(HumanoidArm.LEFT);
        ModelPart rightLeg = getLeg(HumanoidArm.RIGHT);
        
        if (baseMode == VisibilityMode.ALL) {
            head.visible = setVisible;
            torso.visible = setVisible;
            leftLeg.visible = setVisible;
            rightLeg.visible = setVisible;
            leftArm.visible = setVisible;
            rightArm.visible = setVisible;
        }
        else {
            head.visible = !setVisible;
            torso.visible = !setVisible;
            leftLeg.visible = !setVisible;
            rightLeg.visible = !setVisible;
            switch (baseMode) {
            case ARMS_ONLY:
                leftArm.visible = setVisible;
                rightArm.visible = setVisible;
                break;
            case LEFT_ARM_ONLY:
                leftArm.visible = setVisible;
                rightArm.visible = !setVisible;
                break;
            case RIGHT_ARM_ONLY:
                leftArm.visible = !setVisible;
                rightArm.visible = setVisible;
                break;
            case NONE:
                leftArm.visible = !setVisible;
                rightArm.visible = !setVisible;
            default:
                break;
            }
        }
    }

    @Override
    protected void partMissing(StandPart standPart) {
        switch (standPart) {
        case MAIN_BODY:
            head.visible = false;
            torso.visible = false;
            break;
        case ARMS:
            getArm(HumanoidArm.LEFT).visible = false;
            getArm(HumanoidArm.RIGHT).visible = false;
            break;
        case LEGS:
            getLeg(HumanoidArm.LEFT).visible = false;
            getLeg(HumanoidArm.RIGHT).visible = false;
            break;
        }
    }
    
    
    public void addCrumbleParticleAt(HumanoidPart humanoidPart, ResourceLocation texture, Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        StandCrumbleParticle particle = new StandCrumbleParticle(mc.level, pos.x, pos.y, pos.z, 0, 0, 0);
        
        ModelPart mainPart;
        switch (humanoidPart) {
        case HEAD: 
            mainPart = head;
            break;
        case TORSO: 
            mainPart = torso;
            break;
        case LEFT_ARM: 
            mainPart = leftArm;
            break;
        case RIGHT_ARM: 
            mainPart = rightArm;
            break;
        case LEFT_LEG: 
            mainPart = leftLeg;
            break;
        case RIGHT_LEG: 
            mainPart = rightLeg;
            break;
        default:
            throw new IllegalArgumentException();
        }
        Random random = new Random();
        List<ModelPart> allModelParts = new ArrayList<>();
        addChildren(mainPart, allModelParts);
        ModelPart randomPart = allModelParts.get(random.nextInt(allModelParts.size()));
        // 1.20.1 keeps the cuboids in a plain list, 1.16.5 used a fastutil ObjectList
        List<ModelPart.Cube> cubes = randomPart.cubes;
        if (!cubes.isEmpty()) {
            ModelPart.Cube cube = cubes.get(random.nextInt(cubes.size()));
            ModelPart.Polygon[] polygons = cube.polygons;
            ModelPart.Polygon polygon = polygons[random.nextInt(polygons.length)];
            if (polygon != null) {
                ModelPart.Vertex[] vertices = polygon.vertices;
                if (vertices.length > 0) {
                    float u0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).min().getAsDouble();
                    float v0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).min().getAsDouble();
                    float u1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).max().getAsDouble();
                    float v1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).max().getAsDouble();
                    particle.setTextureAndUv(texture, u0, v0, u1, v1);
                    mc.particleEngine.add(particle);
                }
            }
        }
    }
    
    private void addChildren(ModelPart parent, Collection<ModelPart> collection) {
        collection.add(parent);
        for (net.minecraft.client.model.geom.ModelPart child : parent.children.values()) { // 1.20.1 children are a name-keyed map
            addChildren((ModelPart) child, collection);
        }
    }
    
    private enum HumanoidPart {
        HEAD,
        TORSO,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG;
    }
    
    
    
    @Override
    @Deprecated
    protected void initActionPoses() {
        super.initActionPoses();
        
        RotationAngle[] jabRightAngles1 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -15, 0),
                RotationAngle.fromDegrees(leftArm, -7.5F, 0, -15),
                RotationAngle.fromDegrees(leftForeArm, -100, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -105, 0, -15)
        };
        RotationAngle[] jabRightAngles2 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -20F, 0),
                RotationAngle.fromDegrees(leftArm, 30F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -107.5F, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        RotationAngle[] jabRightAngles3 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -12.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -17.5F, 0),
                RotationAngle.fromDegrees(leftArm, 37.5F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -115, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, -81.9244F, 11.0311F, 70.2661F),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        RotationAngle[] jabRightAngles4 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -3.75F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -3.75F, 0),
                RotationAngle.fromDegrees(leftArm, 5.63F, 0, -20.62F),
                RotationAngle.fromDegrees(leftForeArm, -103.75F, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        
        ModelAnim<T> armsRotation = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            float xRot = xRotRad * rotationAmount;
            setSecondXRot(leftArm, xRot);
            setSecondXRot(rightArm, xRot);
        };
        
        ModelAnim<T> armsRotationFull = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            setSecondXRot(leftArm, xRotRad);
            setSecondXRot(rightArm, xRotRad);
        };
        
        ModelAnim<T> armsRotationBack = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            float xRot = xRotRad * (1 - rotationAmount);
            setSecondXRot(leftArm, xRot);
            setSecondXRot(rightArm, xRot);
        };
        
        IModelPose<T> jabStart = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles1)),
                new ModelPose<T>(jabRightAngles1));
        
        IModelPose<T> jabArmTurn = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles2)).setAdditionalAnim(armsRotation),
                new ModelPose<T>(jabRightAngles2).setAdditionalAnim(armsRotation));
        
        IModelPose<T> jabImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles3)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(jabRightAngles3).setAdditionalAnim(armsRotationFull)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabArmTurnBack = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles4)).setAdditionalAnim(armsRotationBack),
                new ModelPose<T>(jabRightAngles4).setAdditionalAnim(armsRotationBack)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabEnd = new ModelPoseSided<>(
                new ModelPose<T>(jabRightAngles1),
                new ModelPose<T>(mirrorAngles(jabRightAngles1)));
        
        actionAnim.putIfAbsent(StandPose.LIGHT_ATTACK, 
                new PosedActionAnimation.Builder<T>()
                
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<T>(jabStart)
                        .addPose(0.5F, jabArmTurn)
                        .addPose(0.75F, jabImpact)
                        .build(jabImpact))
                
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<T>(jabImpact)
                        .addPose(0.25F, jabImpact)
                        .addPose(0.5F, jabArmTurnBack)
                        .build(jabEnd))
                
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<T>(jabEnd)
                        .addPose(0.75F, jabEnd)
                        .build(idlePose))
                
                .build(idlePose));

        
        
        RotationAngle[] heavyRightStart = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 15, 0),
                RotationAngle.fromDegrees(upperPart, 0, 15, 0),
                RotationAngle.fromDegrees(leftArm, -90, 0, -90),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 60),
                RotationAngle.fromDegrees(rightForeArm, -135, 0, 0)
        };
        RotationAngle[] heavyRightBackswing = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 26.25F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 26.25F, 0),
                RotationAngle.fromDegrees(leftArm, -67.5F, 0, -90),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 30, 0, 60),
                RotationAngle.fromDegrees(rightForeArm, -120, 0, 0)
        };
        RotationAngle[] heavyRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -26.25F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -26.25F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, -135, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, -67.5F, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        
        IModelPose<T> heavyStart = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightStart)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightStart).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> heavyBackswing = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightBackswing)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightBackswing).setAdditionalAnim(armsRotationFull)).setEasing(sw -> sw * sw);
        
        IModelPose<T> heavyImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightImpact)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightImpact).setAdditionalAnim(armsRotationFull)).setEasing(sw -> sw * sw * sw);
        
        PosedActionAnimation<T> heavyAttackAnim = new PosedActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<T>(heavyStart)
                        .addPose(0.95F, heavyBackswing)
                        .build(heavyImpact))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<T>(heavyImpact, idlePose)
                        .setEasing(pr -> Math.max(2F * (pr - 1) + 1, 0F)))
                .build(idlePose);
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK, heavyAttackAnim);
        
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK_FINISHER, heavyAttackAnim);
        
        
        
        actionAnim.putIfAbsent(StandPose.BLOCK, new PosedActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<T>(new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(rightForeArm, -90, 30, -90),
                        RotationAngle.fromDegrees(leftForeArm, -90, -30, 90)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
                    float blockXRot = Mth.clamp(xRotRad, -60 * MathUtil.DEG_TO_RAD, 60 * MathUtil.DEG_TO_RAD) / 2;
                    rightArm.xRot = -1.5708F + blockXRot;
                    leftArm.xRot = rightArm.xRot;

                    rightArm.yRot = -blockXRot / 2;
                    leftArm.yRot = -rightArm.yRot;

                    rightArm.zRot = -Math.abs(blockXRot) / 2 + 0.7854F;
                    leftArm.zRot = -rightArm.zRot;
                }))
                .build(idlePose));
        
        

        RotationAngle[] barrageRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -30, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, -135, 0, 0),
                RotationAngle.fromDegrees(rightArm, -90, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        
        IModelPose<T> barrageHitStart = new ModelPoseSided<>(
                new ModelPose<T>(barrageRightImpact).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> barrageHitImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(barrageRightImpact).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> barrageRecovery = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -22.5F),
                RotationAngle.fromDegrees(leftForeArm, -75, 7.5F, 22.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -75, -7.5F, -22.5F)
        });
        
        actionAnim.putIfAbsent(StandPose.BARRAGE, new StandTwoHandedBarrageAnimation<T>(this, 
                new ModelPoseTransition<T>(barrageHitStart, barrageHitImpact).setEasing(HumanoidStandModel::barrageHitEasing), 
                new ModelPoseTransitionMultiple.Builder<T>(new ModelPose<T>(
                        RotationAngle.fromDegrees(body, 0, 0, 0),
                        RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                        RotationAngle.fromDegrees(leftArm, -33.75F, 0, -75),
                        RotationAngle.fromDegrees(leftForeArm, -67.5F, 0, 0),
                        RotationAngle.fromDegrees(rightArm, -33.75F, 0, 75),
                        RotationAngle.fromDegrees(rightForeArm, -67.5F, 0, 0)).setAdditionalAnim(armsRotationFull))
                .addPose(0.25F, barrageRecovery)
                .addPose(0.5F, barrageRecovery)
                .build(idlePose)));
    }
    
    public static float barrageHitEasing(float loopProgress) {
        if (loopProgress < 0.5F) {
            return loopProgress * loopProgress * loopProgress * 8;
        }
        if (loopProgress < 1.0F) {
            float halfSw = 2 * loopProgress - 1;
            return 1 - halfSw * halfSw * halfSw;
        }
        return 0F;
    }
    
    protected RotationAngle[] mirrorAngles(RotationAngle[] angles) {
        RotationAngle[] mirrored = new RotationAngle[angles.length];
        for (int i = 0; i < angles.length; i++) {
            RotationAngle angle = angles[i];
            mirrored[i] = new RotationAngle(getOppositeHandside(angle.modelRenderer), angle.angleX, -angle.angleY, -angle.angleZ);
        }
        return mirrored;
    }
    
    
    @Override
    public ModelPart getArm(HumanoidArm side) {
        switch (side) {
        case LEFT:
            return leftArmXRot != null ? leftArmXRot : leftArm;
        case RIGHT:
            return rightArmXRot != null ? rightArmXRot : rightArm;
        }
        return null;
    }
    
    @Override
    public ModelPart getArmNoXRot(HumanoidArm side) {
        switch (side) {
        case LEFT:
            return leftArm != null ? leftArm : leftArm;
        case RIGHT:
            return rightArm != null ? rightArm : rightArm;
        }
        return null;
    }
    
    protected ModelPart getForeArm(HumanoidArm side) {
        switch (side) {
        case LEFT:
            return leftForeArm;
        case RIGHT:
            return rightForeArm;
        }
        return null;
    }
    
    public ModelPart getHead() {
        return head;
    }
    
    public ModelPart getTorso() {
        return torso;
    }
    
    public ModelPart getLeg(HumanoidArm side) {
        switch (side) {
        case LEFT:
            return leftLegXRot != null ? leftLegXRot : leftLeg;
        case RIGHT:
            return rightLegXRot != null ? rightLegXRot : rightLeg;
        }
        return null;
    }
    
    public ModelPart getLegNoXRot(HumanoidArm side) {
        switch (side) {
        case LEFT:
            return leftLeg != null ? leftLeg : leftLeg;
        case RIGHT:
            return rightLeg != null ? rightLeg : rightLeg;
        }
        return null;
    }
    
    
    @Override
    protected ModelPose<T> initPoseReset() {
        return new ModelPose<T>(
                new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0, 0, 0),
                        new RotationAngle(torso, 0, 0, 0),
                        new RotationAngle(rightArm, 0, 0, 0),
                        new RotationAngle(rightForeArm, 0, 0, 0),
                        new RotationAngle(leftArm, 0, 0, 0),
                        new RotationAngle(leftForeArm, 0, 0, 0),
                        new RotationAngle(rightLeg, 0, 0, 0),
                        new RotationAngle(rightLowerLeg, 0, 0, 0),
                        new RotationAngle(leftLeg, 0, 0, 0),
                        new RotationAngle(leftLowerLeg, 0, 0, 0)
                });
    }

    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        
        if (ClientModSettings.getSettingsReadOnly().standMotionTilt) {
            motionTilt(entity, this, ticks);
        }

        if (!usesGeckoAnims()) {
            rotateJoint(leftArmJoint, leftForeArm);
            rotateJoint(rightArmJoint, rightForeArm);
            rotateJoint(leftLegJoint, leftLowerLeg);
            rotateJoint(rightLegJoint, rightLowerLeg);
        }
    }

    private static final int TICKS_MOTION_TILT_LERP = 5;
    public static void motionTilt(StandEntity entity, HumanoidStandModel<?> model, float ticks) {
        if (entity.getStandPose() != StandPose.SUMMON) {
            Vec3 tiltVec;
            List<Vec3> vecQueue = entity.tiltVecQueue;
            while (vecQueue.size() > TICKS_MOTION_TILT_LERP) vecQueue.remove(vecQueue.size() - 1);
            boolean fillQueue = vecQueue.size() < TICKS_MOTION_TILT_LERP;
            if (fillQueue || Mth.floor(entity.lastMotionTiltTick) != Mth.floor(ticks)) {
                Vec3 motion = entity.position().subtract(entity.xOld, entity.yOld, entity.zOld);
                
                tiltVec = motion.yRot(entity.yBodyRot * MathUtil.DEG_TO_RAD).scale(2);
                tiltVec = new Vec3(tiltVec.z, 0, tiltVec.x);
                double motionSqr = tiltVec.lengthSqr();
                if (motionSqr > Math.pow(Math.PI / 4, 2)) {
                    tiltVec = tiltVec.normalize().scale(Math.PI / 4);
                }
                
                if (fillQueue) {
                    for (int i = vecQueue.size(); i < TICKS_MOTION_TILT_LERP; i++) {
                        vecQueue.add(tiltVec);
                    }
                }
                else {
                    vecQueue.remove(0);
                    vecQueue.add(tiltVec);
                }
                
                entity.lastMotionTiltTick = ticks;
            }
            
            float partialTick = Mth.frac(ticks);
            tiltVec = lerpVecs(vecQueue, partialTick);
            
            double tiltSqr = tiltVec.lengthSqr();
            if (tiltSqr > 1.0E-4) {
                double tilt = Math.sqrt(tiltSqr);
                double d1 = Mth.clamp(1 - tilt / Math.PI * 4, 0, 1);
                boolean idlePose = entity.getStandPose() == StandPose.IDLE;
                
                float tiltX = (float) tiltVec.x;
                float bodyTiltX = tiltX * 0.75f;
                float legsTiltX = tiltX - bodyTiltX;

                model.body.xRot += bodyTiltX;
                if (idlePose) {
                    model.body.zRot += tiltVec.z;
                    model.body.yRot *= d1;
                }

                double d = Mth.clamp(1 - 1.5 * tilt / Math.PI, 0, 1);
                model.leftLowerLeg.xRot *= d;
                model.rightLowerLeg.xRot *= d;
                model.leftLowerLeg.yRot *= d;
                model.rightLowerLeg.yRot *= d;
                model.leftLowerLeg.zRot *= d;
                model.rightLowerLeg.zRot *= d;
                if (idlePose) {
                    model.leftForeArm.xRot *= d;
                    model.rightForeArm.xRot *= d;
                    model.leftForeArm.yRot *= d;
                    model.rightForeArm.yRot *= d;
                    model.leftForeArm.zRot *= d;
                    model.rightForeArm.zRot *= d;
                }
                
                double d2 = Mth.clamp(1 - tilt / (2 * Math.PI), 0, 1);
                if (idlePose) {
                    model.leftArm.xRot *= d2;
                    model.rightArm.xRot *= d2;
                    model.leftArm.yRot *= d2;
                    model.rightArm.yRot *= d2;
                    model.leftArm.zRot *= d2;
                    model.rightArm.zRot *= d2;
                }
                else {
                    model.addSecondXRot(model.leftArm, (float) -bodyTiltX);
                    model.addSecondXRot(model.rightArm, (float) -bodyTiltX);
                    if (model.leftArmXRot != null) model.leftArmXRot.xRot -= bodyTiltX;
                    if (model.rightArmXRot != null) model.rightArmXRot.xRot -= bodyTiltX;
                }
                
                model.leftLeg.xRot *= d2;
                model.rightLeg.xRot *= d2;
                model.leftLeg.yRot *= d2;
                model.rightLeg.yRot *= d2;
                model.leftLeg.zRot *= d2;
                model.rightLeg.zRot *= d2;
                
                model.addSecondXRot(model.leftLeg, (float) legsTiltX);
                model.addSecondXRot(model.rightLeg, (float) legsTiltX);
                if (model.leftLegXRot != null) model.leftLegXRot.xRot += legsTiltX;
                if (model.rightLegXRot != null) model.rightLegXRot.xRot += legsTiltX;
            }
        }
    }
    
    private static Vec3 lerpVecs(List<Vec3> vecs, float partialTick) {
        double x = 0;
        double y = 0;
        double z = 0;
        Vec3 prevVec = vecs.get(0);
        Vec3 vec;
        float n = vecs.size();
        for (int i = 1; i < n; i++) {
            vec = vecs.get(i);
            x += Mth.lerp(partialTick, prevVec.x, vec.x);
            y += Mth.lerp(partialTick, prevVec.y, vec.y);
            z += Mth.lerp(partialTick, prevVec.z, vec.z);
            prevVec = vec;
        }
        return new Vec3(x / n, y / n, z / n);
    }

    protected void rotateJoint(ModelPart joint, ModelPart limbPart) {
        if (joint != null) {
            joint.xRot = limbPart.xRot / 2;
            joint.yRot = limbPart.yRot / 2;
            joint.zRot = limbPart.zRot / 2;
        }
    }

    @Override
    public Iterable<net.minecraft.client.model.geom.ModelPart> headParts() {
        return ImmutableList.of(head);
    }

    @Override
    public Iterable<net.minecraft.client.model.geom.ModelPart> bodyParts() {
        return ImmutableList.of(body);
    }
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftArm, rightArm);
        oppositeHandside.put(leftForeArm, rightForeArm);
        oppositeHandside.put(leftLeg, rightLeg);
        oppositeHandside.put(leftLowerLeg, rightLowerLeg);
    }
    
    @Override
    public void translateToHand(HumanoidArm handSide, PoseStack matrixStack) {
        matrixStack.translate(handSide == HumanoidArm.LEFT ? -0.0625 : 0.0625, 0, 0);
        body.translateAndRotate(matrixStack);
        upperPart.translateAndRotate(matrixStack);
        
        ModelPart arm = getArm(handSide);
        arm.translateAndRotate(matrixStack);

        ModelPart foreArm = getForeArm(handSide);
        foreArm.translateAndRotate(matrixStack);
        matrixStack.translate(
                (double)(-foreArm.x / 16.0F), 
                (double)(-foreArm.y / 16.0F), 
                (double)(-foreArm.z / 16.0F));
    }
    
    
    protected Map<TargetHitPart, List<ModelPart.Cube>> cubesCache;
    @Override
    public ModelPart.Cube getRandomCubeAt(TargetHitPart entityPart) {
        if (cubesCache == null) {
            cacheCubes();
        }
        List<ModelPart.Cube> cubes = cubesCache.get(entityPart);
        if (cubes != null && !cubes.isEmpty()) {
            return cubes.get(RANDOM.nextInt(cubes.size()));
        }
        return null;
    }
    
    protected void cacheCubes() {
        cubesCache = new EnumMap<>(TargetHitPart.class);
        List<ModelPart> headParts = new ArrayList<>();
        List<ModelPart> legsParts = new ArrayList<>();
        List<ModelPart> middleParts = new ArrayList<>();
        addChildrenRecursive(head, headParts);
        addChildrenRecursive(leftLeg, legsParts);
        addChildrenRecursive(rightLeg, legsParts);
        addChildrenRecursive(torso, middleParts);
        addChildrenRecursive(leftArm, middleParts);
        addChildrenRecursive(rightArm, middleParts);
        cubesCache.put(TargetHitPart.HEAD, allCubes(headParts));
        cubesCache.put(TargetHitPart.TORSO_ARMS, allCubes(middleParts));
        cubesCache.put(TargetHitPart.LEGS, allCubes(legsParts));
    }
    
    public static void addChildrenRecursive(ModelPart modelPart, Collection<ModelPart> collection) {
        collection.add(modelPart);
        for (net.minecraft.client.model.geom.ModelPart child : modelPart.children.values()) { // 1.20.1 children are a name-keyed map
            addChildrenRecursive((ModelPart) child, collection);
        }
    }
    
    public static List<ModelPart.Cube> allCubes(List<ModelPart> modelParts) {
        List<ModelPart.Cube> cubes = modelParts.stream()
                .flatMap(modelPart -> modelPart.cubes.stream())
                .collect(Collectors.toList());
        return cubes;
    }
    
    // TODO select quads with weight depending on their size
    public static ModelPart.Polygon getRandomQuad(ModelPart.Cube cube) {
        if (cube == null) return null;
        ModelPart.Polygon[] polygons = cube.polygons;
        ModelPart.Polygon polygon = polygons[RANDOM.nextInt(polygons.length)];
        return polygon;
    }
    
}