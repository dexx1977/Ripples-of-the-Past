package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.HamonRebuffOverdrive;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.particle.custom.IFirstPersonParticle;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler.BendablePart;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.EnergyRippleLayer.HamonEnergyRippleHandler.SparkPseudoParticle;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.HumanoidModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class EnergyRippleLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();

    public EnergyRippleLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }
    
    // FIXME refactor this to be able to add particles from the abilities themselves
    private static void addHamonSparks(LivingEntity entity, HamonData hamon, HumanoidModel<?> model, float timeDelta, HamonEnergyRippleHandler sparks) {
        float handSparkIntensity = 4 + hamon.getHamonStrengthLevelRatio() * 12;
        int particles = MathUtil.fractionRandomInc(handSparkIntensity * timeDelta);
        if (particles > 0) {
            float controlLevel = hamon.getHamonControlLevelRatio();
            Optional<ContinuousActionInstance<?, ?>> curPlayerAction = ContinuousActionInstance.getCurrentAction(entity);
            
            // hand sparks (wall climbing, hamon shock)
            ParticleType<?> particle = null;
            if (LivingWallClimbing.getHandler(entity).map(LivingWallClimbing::isHamon).orElse(false)
                    || curPlayerAction.map(action -> action.getAction() == ModHamonActions.HAMON_SHOCK.get()).orElse(false)) {
                particle = ModParticles.HAMON_SPARK.get();
            }
            if (particle != null) {
                for (HumanoidArm hand : HumanoidArm.values()) {
                    for (int i = 0; i < particles; i++) {
                        Vec3 offset = new Vec3(
                                (RANDOM.nextDouble() - 0.5) * 0.4,
                                RANDOM.nextDouble() * (0.5 - 0.4 * controlLevel) - 0.65,
                                (RANDOM.nextDouble() - 0.5) * 0.4);
                        sparks.addSpark(SparkPseudoParticle.armSpark(model, hand == HumanoidArm.RIGHT, particle, offset));
                    }
                }
            }
            
            // elbow sparks (rebuff overdrive)
            curPlayerAction
            .filter(action -> action.getAction() == ModHamonActions.JOSEPH_REBUFF_OVERDRIVE.get())
            .map(action -> (HamonRebuffOverdrive.Instance) action)
            .ifPresent(rebuff -> {
                if (rebuff.addSparksThisTick()) {
                    int rebuffParticles = MathUtil.fractionRandomInc(12 + hamon.getHamonStrengthLevelRatio() * 12 * timeDelta);
                    for (HumanoidArm hand : HumanoidArm.values()) {
                        for (int i = 0; i < rebuffParticles; i++) {
                            Vec3 offset = new Vec3(
                                    (RANDOM.nextDouble() - 0.5) * 0.4,
                                    RANDOM.nextDouble() * (0.5 - 0.4 * 0) - 0.45,
                                    (RANDOM.nextDouble() - 0.5) * 0.4);
                            sparks.addSpark(SparkPseudoParticle.armSpark(model, hand == HumanoidArm.RIGHT, ModParticles.HAMON_SPARK.get(), offset));
                        }
                    }
                }
            });
            
            // knee sparks (sendo wave kick)
            if (GeneralUtil.orElseFalse(ContinuousActionInstance.getCurrentAction(entity), 
                    action -> action.getAction() == ModHamonActions.ZEPPELI_SENDO_WAVE_KICK.get())) {
                for (int i = 0; i < particles; i++) {
                    Vec3 offset = new Vec3(
                            (RANDOM.nextDouble() - 0.5) * 0.4,
                            (RANDOM.nextDouble() - 0.5) * (0.3 - 0.2 * controlLevel) - 0.375,
                            (RANDOM.nextDouble()) * 0.2);
                    sparks.addSpark(SparkPseudoParticle.legSpark(model, true, ModParticles.HAMON_SPARK.get(), offset));
                }
            }
        }
    }
    
    private static void addHamonSparkWaves(LivingEntity entity, float time, float timeDelta, HamonEnergyRippleHandler sparks) {
        int t1 = (int) (time / HamonEnergyRippleHandler.WAVES_GAP);
        int t2 = (int) ((time - timeDelta) / HamonEnergyRippleHandler.WAVES_GAP);
        int waves = t1 - t2;
        if (waves > 0) {
            ParticleType<?> particle = null;
            Action<?> heldAction = INonStandPower.getNonStandPowerOptional(entity).resolve().map(IPower::getHeldAction).orElse(null);;
            IPlayerAction<?, ?> curAction = ContinuousActionInstance.getCurrentAction(entity).map(instance -> instance.getAction()).orElse(null);

            if (heldAction == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get() || 
                    curAction == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()) {
                particle = ModParticles.HAMON_SPARK_YELLOW.get();
            }
            
            if (particle != null) {
                for (int i = 0; i < waves; i++) {
                    sparks.addSparkWave(new HamonEnergyRippleHandler.SparkWave(time - HamonEnergyRippleHandler.WAVES_GAP * (t2 + i + 1), particle));
                }
            }
        }
    }
    
    
    
    
    
    @SuppressWarnings("deprecation")
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        Optional<HamonData> hamon = INonStandPower.getNonStandPowerOptional(entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
        if (!hamon.isPresent()) return;
        HamonEnergyRippleHandler sparksHandler = getSparksHandler(entity);
        if (sparksHandler == null) return;
        
        M model = getParentModel();
        sparksHandler.updateSparks(ticks, model, hamon.get());
        
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        
        
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        HamonEnergyRippleHandler.SparkPseudoParticle.RENDER_TYPE.begin(bufferBuilder, mc.textureManager);

        Vector3f[] avector3f = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F), 
                new Vector3f(-1.0F, 1.0F, 0.0F), 
                new Vector3f(1.0F, 1.0F, 0.0F), 
                new Vector3f(1.0F, -1.0F, 0.0F)};
        float yBodyRot = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        
        for (int i = 0; i < 4; ++i) {
           Vector3f vector3f = avector3f[i];
           vector3f.add(-0.125F, 0, -0.125F);
           vector3f.rotate(Axis.XP.rotationDegrees(-camera.getXRot()));
           vector3f.rotate(Axis.YP.rotationDegrees(180 + camera.getYRot() - yBodyRot));
        }
        
        sparksHandler.render(matrixStack, bufferBuilder, partialTick, avector3f);
        
        HamonEnergyRippleHandler.SparkPseudoParticle.RENDER_TYPE.end(tessellator);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        
        sparksHandler.postRender(ticks);
    }

    private static HamonEnergyRippleHandler getSparksHandler(LivingEntity entity) {
        return entity.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getHamonSparkWaves()).orElse(null);
    }
    
    @Nullable
    public static Collection<HamonEnergyRippleHandler.SparkPseudoParticle> getSparksToRenderFirstPerson(Player entity, HumanoidArm hand) {
        Optional<HamonData> hamon = INonStandPower.getNonStandPowerOptional(entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
        if (!hamon.isPresent()) return null;
        HamonEnergyRippleHandler sparksHandler = getSparksHandler(entity);
        if (sparksHandler == null) return null;
        
        
        
        float partialTick = ClientUtil.getPartialTick();
        float ticks = entity.tickCount + partialTick;
        HumanoidModel<?> model = (HumanoidModel<?>) ((LivingEntityRenderer<?, ?>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity)).getModel();
        sparksHandler.updateSparks(ticks, model, hamon.get());
        sparksHandler.postRender(ticks);
        
        
        
        List<HamonEnergyRippleHandler.SparkPseudoParticle> sparks = sparksHandler.sparks.get(
                hand == HumanoidArm.LEFT ? BipedModelPart.LEFT_ARM : BipedModelPart.RIGHT_ARM);
        return sparks;
    }
    
    
    
    public static class HamonEnergyRippleHandler {
        private final LivingEntity entity;
        private float ticksPrev = Float.MAX_VALUE;
        private final Collection<SparkWave> sparkWaves = new LinkedList<>();
        private final Map<BipedModelPart, List<SparkPseudoParticle>> sparks = Util.make(new EnumMap<>(BipedModelPart.class), map -> {
            for (BipedModelPart key : BipedModelPart.values()) {
                map.put(key, new ArrayList<>());
            }
        });
        
        public HamonEnergyRippleHandler(LivingEntity entity) {
            this.entity = entity;
        }
        
        public void updateSparks(float time, HumanoidModel<?> model, HamonData entityHamon) {
            if (Minecraft.getInstance().isPaused()) return;
            float delta = time - ticksPrev;
            if (delta < 0) return;
            
            Iterator<SparkWave> wIt = sparkWaves.iterator();
            while (wIt.hasNext()) {
                SparkWave wave = wIt.next();
                if (wave.addParticles(delta, model, entity, this)) {
                    wIt.remove();
                }
            }
            
            addHamonSparkWaves(entity, time, delta, this);
            addHamonSparks(entity, entityHamon, model, delta, this);
        }
        
        void addSparkWave(SparkWave wave) {
            sparkWaves.add(wave);
        }
        
        void addSpark(SparkPseudoParticle spark) {
            sparks.get(spark.modelPartType).add(spark);
        }
        
        public void postRender(float time) {
            float delta = time - ticksPrev;
            ticksPrev = time;
            
            for (List<SparkPseudoParticle> sparksPart : sparks.values()) {
                Iterator<SparkPseudoParticle> spIt = sparksPart.iterator();
                while (spIt.hasNext()) {
                    SparkPseudoParticle spark = spIt.next();
                    if (spark.addTimeDelta(delta)) {
                        spIt.remove();
                    }
                }
            }
        }

        public void render(PoseStack matrixStack, BufferBuilder bufferBuilder, float partialTick, Vector3f[] avector3f) {
            sparks.values().forEach(list -> list.forEach(spark -> {
                matrixStack.pushPose();
                
                spark.translateTo(matrixStack, spark.modelPart, spark.x, spark.y, spark.z);
                matrixStack.scale(spark.scale, spark.scale, spark.scale);
                spark.renderSprite(matrixStack.last().pose(), bufferBuilder, 
                        HamonEnergyRippleHandler.SparkPseudoParticle.PARTICLE_LIGHT, partialTick, avector3f);
                
                matrixStack.popPose();
            }));
        }
        
        

        private static final float SPARK_LIFE_SPAN = 2F;
        private static final float WAVES_GAP = 7.5F;
        private static final float WAVE_DURATION = 20F;
        private static final float SPARKS_PER_TICK = 15F;
        private static class SparkWave {
            private final ParticleType<?> particleType;
            private float progress;
            
            private SparkWave(float startingDelta, ParticleType<?> particleType) {
                this.progress = startingDelta / WAVE_DURATION;
                this.particleType = particleType;
            }
            
            private boolean addParticles(float timeDelta, HumanoidModel<?> model, LivingEntity entity, HamonEnergyRippleHandler sparks) {
                double sparkCount = timeDelta * SPARKS_PER_TICK;
                if (0.5F < progress) {
                    sparkCount *= (1 + (progress - 0.5F) / (1 - 0.5F)) * 3;
                }
                int sparkCountInt = MathUtil.fractionRandomInc(sparkCount);
                for (int i = 0; i < sparkCountInt; i++) {
                    if (progress <= 0.25F) {
                        double y = (progress / 0.25F - 1) * 0.75;
                        sparks.addSpark(SparkPseudoParticle.legSpark(model, RANDOM.nextBoolean(), 
                                particleType, randomSideOffset(0.25, y, 0.25)));
                    }
                    else if (/*0.25F < */ progress <= 0.5F) {
                        double y = ((progress - 0.25F) / (0.5F - 0.25F) - 1) * 0.75;
                        sparks.addSpark(SparkPseudoParticle.torsoSpark(model, 
                                particleType, randomSideOffset(0.5, y, 0.25)));
                        sparks.addSpark(SparkPseudoParticle.armSpark(model, entity.getMainArm() == HumanoidArm.LEFT, 
                                particleType, randomSideOffset(0.25, y + 0.15, 0.25)));
                        if (0.3333F <= progress) {
                            sparks.addSpark(SparkPseudoParticle.headSpark(model, 
                                    particleType, randomSideOffset(0.5, y, 0.5)));
                        }
                    }
                    else /*if (0.5F < progress)*/ {
                        double r = (progress - 0.5F) / (1 - 0.5F);
                        double y = r * -0.75 + 0.15 + (RANDOM.nextDouble() - 0.5) * 0.375 * r;
                        y = Math.max(y, -0.625);

                        sparks.addSpark(SparkPseudoParticle.armSpark(model, entity.getMainArm() == HumanoidArm.RIGHT, 
                                particleType, randomSideOffset(0.25, y, 0.25)));
                    }
                }
                return (progress += timeDelta / WAVE_DURATION) >= 1;
            }
        }
        
        private static Vec3 randomSideOffset(double widthX, double y, double widthZ) {
            int side = RANDOM.nextDouble() * (widthX + widthZ) < widthX ? 0 : 1;
            if (RANDOM.nextBoolean()) side += 2;
            widthX += 0.05;
            widthZ += 0.05;
            switch (side) {
            case 0:
                return new Vec3((RANDOM.nextDouble() - 0.5) * widthX, y, widthZ / 2);
            case 1:
                return new Vec3(widthX / 2, y, (RANDOM.nextDouble() - 0.5) * widthZ);
            case 2:
                return new Vec3((RANDOM.nextDouble() - 0.5) * widthX, y, -widthZ / 2);
            case 3:
                return new Vec3(-widthX / 2, y, (RANDOM.nextDouble() - 0.5) * widthZ);
            default: // that's simply not possible
                return null;
            }
        }
        
        // FIXME wrong for legs
        private static Vec3 bendOffset(Vec3 pos, HumanoidModel<?> model, BipedModelPart bendablePart, double bendYPoint) {
            if (bendablePart == BipedModelPart.LEFT_LEG || bendablePart == BipedModelPart.RIGHT_LEG) {
                pos = bendOffset(pos, model, BipedModelPart.TORSO, bendYPoint + 0.75);
            }
            
            if (pos.y < bendYPoint && bendablePart.bendable != null) {
                float[] bend = PlayerAnimationHandler.getPlayerAnimator().getBend(model, bendablePart.bendable);
                if (bend[0] != 0) {
                    switch (bendablePart) {
                    case TORSO:
                        pos = pos.add(0, -bendYPoint, 0).xRot(bend[0]).add(0, bendYPoint, 0);
                        break;
                    case LEFT_ARM:
                    case RIGHT_ARM:
                        pos = pos.add(0, -bendYPoint, 0).xRot(-bend[0]).add(0, bendYPoint, 0);
                        break;
                    case LEFT_LEG:
                    case RIGHT_LEG:
                        pos = pos.add(0, -bendYPoint, 0).xRot(-bend[0]).add(0, bendYPoint, 0);
                        break;
                    default:
                        break;
                    }
                }
            }
            return pos;
        }
        
        
        public static class SparkPseudoParticle implements IFirstPersonParticle {
            public static final int PARTICLE_LIGHT = 0xF000F0;
            public static final ParticleRenderType RENDER_TYPE = ParticleRenderType.PARTICLE_SHEET_OPAQUE;
            
            public final TextureAtlasSprite hamonSparkSprite;
            private float age;
            public final float lifeSpan = SPARK_LIFE_SPAN;
            public final BipedModelPart modelPartType;
            public final net.minecraft.client.model.geom.ModelPart modelPart;
            public final double x;
            public final double y;
            public final double z;
            public final float scale;
            
            private SparkPseudoParticle(ParticleType<?> particleType, BipedModelPart modelPartType, 
                    net.minecraft.client.model.geom.ModelPart modelPart, Vec3 pos) {
                this.hamonSparkSprite = CustomParticlesHelper.getSavedSpriteSet(particleType).get(RANDOM);
                this.modelPartType = modelPartType;
                this.modelPart = modelPart;
                this.x = pos.x;
                this.y = pos.y;
                this.z = pos.z;
                this.scale = 0.04F + RANDOM.nextFloat() * 0.02F;
            }
            
            static SparkPseudoParticle legSpark(HumanoidModel<?> model, boolean right, ParticleType<?> particleType, Vec3 offset) {
                BipedModelPart modelPartType = right ? BipedModelPart.RIGHT_LEG : BipedModelPart.LEFT_LEG;
                offset = bendOffset(offset, model, modelPartType, -0.375);
                return new SparkPseudoParticle(particleType, modelPartType, 
                        modelPartType.getModelPart(model), offset);
            }

            static SparkPseudoParticle armSpark(HumanoidModel<?> model, boolean right, ParticleType<?> particleType, Vec3 offset) {
                BipedModelPart modelPartType = right ? BipedModelPart.RIGHT_ARM : BipedModelPart.LEFT_ARM;
                double xPivot = right ? -0.0625 : 0.0625;
                offset = bendOffset(offset, model, modelPartType, -0.225);
                return new SparkPseudoParticle(particleType, modelPartType, 
                        modelPartType.getModelPart(model), offset.add(xPivot, 0, 0));
            }
            
            static SparkPseudoParticle torsoSpark(HumanoidModel<?> model, ParticleType<?> particleType, Vec3 offset) {
                offset = bendOffset(offset, model, BipedModelPart.TORSO, -0.375);
                return new SparkPseudoParticle(particleType, BipedModelPart.TORSO, model.body, offset);
            }
            
            static SparkPseudoParticle headSpark(HumanoidModel<?> model, ParticleType<?> particleType, Vec3 offset) {
                return new SparkPseudoParticle(particleType, BipedModelPart.HEAD, model.head, offset);
            }
            
            private boolean addTimeDelta(float delta) {
                return (age += delta) >= lifeSpan;
            }
            
            @Override
            public void renderSprite(Matrix4f matrixEntry, VertexConsumer buffer, int light, float partialTick, Vector3f[] avector3f) {
                float u0 = hamonSparkSprite.getU0();
                float u1 = hamonSparkSprite.getU1();
                float v0 = hamonSparkSprite.getV0();
                float v1 = hamonSparkSprite.getV1();
                buffer.vertex(matrixEntry, avector3f[0].x(), avector3f[0].y(), avector3f[0].z())
                .uv(u1, v1).color(255, 255, 255, 255).uv2(light).endVertex();
                buffer.vertex(matrixEntry, avector3f[1].x(), avector3f[1].y(), avector3f[1].z())
                .uv(u1, v0).color(255, 255, 255, 255).uv2(light).endVertex();
                buffer.vertex(matrixEntry, avector3f[2].x(), avector3f[2].y(), avector3f[2].z())
                .uv(u0, v0).color(255, 255, 255, 255).uv2(light).endVertex();
                buffer.vertex(matrixEntry, avector3f[3].x(), avector3f[3].y(), avector3f[3].z())
                .uv(u0, v1).color(255, 255, 255, 255).uv2(light).endVertex();
            }

            private void translateTo(PoseStack matrixStack, @Nullable net.minecraft.client.model.geom.ModelPart modelRenderer, double x, double y, double z) {
                if (modelRenderer != null) {
                    modelRenderer.translateAndRotate(matrixStack);
                }

                matrixStack.translate(x, -y, -z);

                if (modelRenderer != null) {
                    if (modelRenderer.xRot != 0.0F) {
                        matrixStack.mulPose(Axis.XP.rotation(-modelRenderer.xRot));
                    }
                    if (modelRenderer.yRot != 0.0F) {
                        matrixStack.mulPose(Axis.YP.rotation(-modelRenderer.yRot));
                    }
                    if (modelRenderer.zRot != 0.0F) {
                        matrixStack.mulPose(Axis.ZP.rotation(-modelRenderer.zRot));
                    }
                }
            }
            
            
            @Override
            public String toString() {
                return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (1,1,1,1), Age " + this.age;
            }
        }
    }
    
    
    private enum BipedModelPart {
        HEAD(null),
        TORSO(BendablePart.TORSO),
        LEFT_ARM(BendablePart.LEFT_ARM),
        RIGHT_ARM(BendablePart.RIGHT_ARM),
        LEFT_LEG(BendablePart.LEFT_LEG),
        RIGHT_LEG(BendablePart.RIGHT_LEG);
        
        public final BendablePart bendable;
        
        private BipedModelPart(BendablePart bendable) {
            this.bendable = bendable;
        }
        
        public net.minecraft.client.model.geom.ModelPart getModelPart(HumanoidModel<?> model) {
            switch (this) {
            case HEAD:
                return model.head;
            case TORSO:
                return model.body;
            case LEFT_ARM:
                return model.leftArm;
            case RIGHT_ARM:
                return model.rightArm;
            case LEFT_LEG:
                return model.leftLeg;
            case RIGHT_LEG:
                return model.rightLeg;
            }
            throw new IllegalStateException();
        }
    }
    
    
    public static Vec3 handTipPos(HumanoidModel<?> posedModel, HumanoidArm hand, Vec3 offset, float yBodyRot) {
        double scale = 0.9375;
        boolean right = hand == HumanoidArm.RIGHT;
        offset = offset.add(0, -0.65, 0);
        
        BipedModelPart modelPartType = right ? BipedModelPart.RIGHT_ARM : BipedModelPart.LEFT_ARM;
        net.minecraft.client.model.geom.ModelPart modelPart = modelPartType.getModelPart(posedModel);
        double xPivot = right ? -0.0625 : 0.0625;
        offset = HamonEnergyRippleHandler.bendOffset(offset, posedModel, modelPartType, -0.225);
        offset = offset.add(xPivot, 0, 0);
        
        
        Vec3 pos = new Vec3(modelPart.x / 16, 1.5 - modelPart.y / 16, modelPart.z / 16);
        if (modelPart.zRot != 0.0F) {
            pos = pos.zRot(-modelPart.zRot);
        }
        if (modelPart.yRot != 0.0F) {
            pos = pos.yRot(modelPart.yRot);
        }
        if (modelPart.xRot != 0.0F) {
            pos = pos.xRot(modelPart.xRot);
        }
        
        pos = pos.add(offset.x, offset.y, offset.z);
        if (modelPart.xRot != 0.0F) {
            pos = pos.xRot(-modelPart.xRot);
        }
        if (modelPart.yRot != 0.0F) {
            pos = pos.yRot(-modelPart.yRot);
        }
        if (modelPart.zRot != 0.0F) {
            pos = pos.zRot(modelPart.zRot);
        }
        
        pos = pos.yRot(-yBodyRot * MathUtil.DEG_TO_RAD);
        pos = pos.scale(scale);
        
        return pos;
    }
}
