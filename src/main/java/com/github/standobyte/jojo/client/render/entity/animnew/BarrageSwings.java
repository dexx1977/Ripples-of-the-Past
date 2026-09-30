package com.github.standobyte.jojo.client.render.entity.animnew;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.animnew.stand.StandActionAnimation;
import com.github.standobyte.jojo.client.render.entity.animnew.stand.StandPoseData;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BarrageSwings {
    private List<BarrageSwing> barrageSwings = new LinkedList<>();
    private float loopLast = -1;

    public void addSwing(BarrageSwing swing) {
        barrageSwings.add(swing);
    }
    
    public void updateSwings(Minecraft mc) {
        if (!mc.isPaused() && !barrageSwings.isEmpty()) {
            float timeDelta = mc.getDeltaFrameTime();
            Iterator<BarrageSwing> iter = barrageSwings.iterator();
            while (iter.hasNext()) {
                BarrageSwing swing = iter.next();
                swing.addDelta(timeDelta);
                if (swing.removeSwing()) {
                    iter.remove();
                }
            }
        }
    }
    
    public Iterable<BarrageSwing> getSwings() {
        return barrageSwings;
    }
    
    public void setLoopCount(float loopCount) {
        this.loopLast = loopCount;
    }
    
    public float getLoopCount() {
        return loopLast;
    }
    
    
    
    
    
    public static final Map<String, AddBarrageSwing> BARRAGE_SWING_TYPES = Util.make(new HashMap<>(), map -> {
        map.put("TWO_HANDED", TwoHandedBarrageLoopSwing::addSwing);
    });
    
    public static <T extends StandEntity> boolean onBarrageAnim(String key, T entity, StandEntityModel<T> model, 
            StandActionAnimation barrageAnim, float entityTicks, float curAnimTime) {
        AddBarrageSwing addSwingFunction = BARRAGE_SWING_TYPES.get(key);
        if (addSwingFunction != null) {
            return addSwingFunction.addSwing(entity, model, entity.getBarrageSwings(), barrageAnim, entityTicks, curAnimTime);
        }
        return false;
    }
    
    @FunctionalInterface
    public static interface AddBarrageSwing {
        <T extends StandEntity> boolean addSwing(T entity, StandEntityModel<T> model, BarrageSwings swings, 
                StandActionAnimation barrageAnim, float entityTicks, float curAnimTimeSecs);
    }
    
    
    public abstract static class BarrageSwing {
        protected static final Random RANDOM = new Random();
        protected StandActionAnimation barrageAnim;
        protected float ticks;
        protected float ticksMax;
        
        public BarrageSwing(StandActionAnimation barrageAnim, float startingAnim, float animMax) {
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
        
        public abstract <T extends StandEntity> void poseAndRender(T entity, StandEntityModel<T> model, 
                PoseStack matrixStack, VertexConsumer buffer, float yRotOffsetDeg, float xRotDeg, 
                int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
    }
    
    
    public static class TwoHandedBarrageLoopSwing extends BarrageSwing {
        protected float animTimeOffset;
        protected final HumanoidArm side;
        protected final Vec3 offset;
        protected final float zRot;
        
        public TwoHandedBarrageLoopSwing(StandActionAnimation barrageAnim, float startingAnim, float animMax, 
                HumanoidArm side, double maxOffset, float animTimeOffset) {
            super(barrageAnim, startingAnim, animMax);
            this.animTimeOffset = animTimeOffset;
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
        
        public static <T extends StandEntity> boolean addSwing(T entity, StandEntityModel<T> model, BarrageSwings swings, 
                StandActionAnimation barrageAnim, float entityTicks, float curAnimTimeSecs) {
            if (!entity.getCurrentTaskPhase().filter(phase -> phase == StandEntityAction.Phase.PERFORM).isPresent()) return false;
            
            float lastLoop = swings.getLoopCount();
            float loopLen = 4;
            float loop = entityTicks / loopLen;
            if (entity.animWasBarraging && loop > lastLoop) {
                float hits = StandStatFormulas.getBarrageHitsPerSecond(entity.getAttackSpeed()) / 20F * Math.min(loop - lastLoop, 1) * loopLen;
                int swingsToAdd = MathUtil.fractionRandomInc(hits);
                if (swingsToAdd > 0) {
                    HumanoidArm side = entity.getPunchingHand();
                    double maxOffset = 1 - entity.getPrecision() / 40;
                    if (entity.getRandom().nextBoolean()) side = side.getOpposite();
                    
                    for (int i = 0; i < swingsToAdd; i++) {
                        float x = ((float) i + (entity.getRandom().nextFloat() - 0.5F) * 0.4F) / swingsToAdd;
                        float f = x * loopLen * 0.5F;
                        float addTime = (side == HumanoidArm.LEFT ? loopLen * 0.5f : 0) + (curAnimTimeSecs - curAnimTimeSecs % loopLen);
                        swings.addSwing(new BarrageSwings.TwoHandedBarrageLoopSwing(barrageAnim, f, loopLen, side, maxOffset, addTime));
                        side = side.getOpposite();
                    }
                }
            }
            swings.setLoopCount(loop);
            return true;
        }
        
        @Override
        public <T extends StandEntity> void poseAndRender(T entity, StandEntityModel<T> model, 
                PoseStack matrixStack, VertexConsumer buffer, float yRotOffsetDeg, float xRotDeg, 
                int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            model.setVisibility(entity, side == HumanoidArm.LEFT ? VisibilityMode.LEFT_ARM_ONLY : VisibilityMode.RIGHT_ARM_ONLY, false);
            float loopCompletion = ticks / ticksMax;
            float zMult = loopCompletion < 0.5 ? loopCompletion * 2 : (1 - loopCompletion) * 2;
            double zAdditional = 0.5 * zMult;
            Vec3 offsetRot = new Vec3(offset.x, -offset.y, offset.z + zAdditional).xRot(xRotDeg * MathUtil.DEG_TO_RAD);
            matrixStack.pushPose();
            matrixStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
            model.resetPose(entity);
            StandPoseData standPose = StandPoseData.start()
                    .standPose(model.standPose)
                    .actionPhase(StandEntityAction.Phase.PERFORM)
                    .animTime(ticks + animTimeOffset)
                    .end();
            barrageAnim.poseStand(entity, model, yRotOffsetDeg, xRotDeg, standPose);
            ModelPart arm = model.getArmNoXRot(side);
            arm.zRot = arm.zRot + zMult * zRot;
            model.applyXRotation();
            model.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.75F);
            matrixStack.popPose();
        }
    }

}
