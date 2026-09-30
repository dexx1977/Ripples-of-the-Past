package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.util.Optional;
import java.util.OptionalInt;

import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.IEntityGlowColor;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.StandGlowLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.StandModelLayerRenderer;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import com.mojang.math.Axis;

public class StandEntityRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends LivingEntityRenderer<T, M> {
    private final ResourceLocation texture;

    public StandEntityRenderer(EntityRendererProvider.Context context, M entityModel, 
            ResourceLocation texture, float shadowRadius) {
        super(context, entityModel, shadowRadius);
        this.texture = texture;
        entityModel.afterInit();
        addLayer(new ItemInHandLayer<>(this));
        addLayer(new StandGlowLayer<>(this, texture));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return getTextureLocation(entity.getStandSkin());
    }
    
    @Deprecated
    @Override
    public M getModel() {
        return CustomResources.getStandModelOverrides().overrideModel(model);
    }
    
    public M getModel(T entity) {
        return getModel(entity.getStandSkin());
    }
    
    public M getModel(Optional<ResourceLocation> standSkin) {
        M model = getModel();
        M skinModel = StandSkinsManager.getInstance().getStandSkin(standSkin).map(
                skin -> (M) skin.standModels.getOrDefault(model.getModelId(), model)).orElse(model);
        return skinModel;
    }
    
    public ResourceLocation getTextureLocation(Optional<ResourceLocation> standSkin) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(standSkin), texture);
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return super.shouldShowName(entity) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    protected boolean isBodyVisible(T entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
    protected boolean visibleForSpectator(T entity) {
        return entity.underInvisibilityEffect() && JojoModUtil.seesInvisibleAsSpectator(Minecraft.getInstance().player);
    }

    protected RenderType getRenderType(T entity, ResourceLocation modelTexture) {
        return renderType(entity, this.model, modelTexture, 
                isBodyVisible(entity), visibleForSpectator(entity), Minecraft.getInstance().shouldEntityAppearGlowing(entity));
    }

    public RenderType getRenderType(T entity, M model, ResourceLocation modelTexture) {
        return renderType(entity, model, modelTexture, 
                isBodyVisible(entity), visibleForSpectator(entity), Minecraft.getInstance().shouldEntityAppearGlowing(entity));
    }

    protected RenderType renderType(T entity, M model, ResourceLocation modelTexture, 
            boolean isVisible, boolean isVisibleForSpectator, boolean isGlowing) {
        if (isVisible || isVisibleForSpectator) {
            return model.renderType(modelTexture);
        }
        return isGlowing ? RenderType.outline(modelTexture) : null;
    }

    @Deprecated
    @Override
    public RenderType getRenderType(T entity, boolean isVisible, boolean isVisibleForSpectator, boolean isGlowing) {
        return renderType(entity, this.model, getTextureLocation(entity), isVisible, isVisibleForSpectator, isGlowing);
    }
    
    protected float calcAlpha(T entity, float partialTick) {
        if (visibleForSpectator(entity)) {
            return 0.15F;
        }
        return entity.getAlpha(partialTick) * viewObstructionPrevention.alphaFactor;
    }
    
    protected ViewObstructionPrevention obstructsView(T entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson() && isBodyVisible(entity)) {
            Entity user = entity.getUser();
            if (mc.cameraEntity != null && mc.cameraEntity.is(user)) {
                if (ClientEventHandler.getInstance().isZooming) {
                    return ClientModSettings.getSettingsReadOnly().standOutline ? ViewObstructionPrevention.ARMS_ONLY_OUTLINE : ViewObstructionPrevention.ARMS_ONLY;
                }
                if (!entity.isArmsOnlyMode()) {
                    Vec3 diffVec = entity.getPosition(partialTick).subtract(user.getPosition(partialTick));
                    Vec3 lookVec = Vec3.directionFromRotation(0, user.getViewYRot(partialTick));
                    
                    diffVec = new Vec3(diffVec.x, 0, diffVec.z);
                    lookVec = new Vec3(lookVec.x, 0, lookVec.z);
                    double distanceSqr = diffVec.lengthSqr();
                    if (distanceSqr < 0.25 || distanceSqr < 9 && lookVec.dot(diffVec) > distanceSqr / 2) {
                        return ClientModSettings.getSettingsReadOnly().standOutline ? ViewObstructionPrevention.ARMS_ONLY_OUTLINE : ViewObstructionPrevention.ARMS_ONLY;
                    }
                }
            }
        }
        return ViewObstructionPrevention.NONE;
    }
    
    protected enum ViewObstructionPrevention {
        NONE(false, false, 1),
        ARMS_ONLY(true, false, 1),
        ARMS_ONLY_OUTLINE(true, true, 1),
        TRANSLUCENCY(false, false, 0.25F);
        
        private final boolean armsOnly;
        private final boolean outline;
        private final float alphaFactor;
        
        private ViewObstructionPrevention(boolean armsOnly, boolean outline, float alphaFactor) {
            this.armsOnly = armsOnly;
            this.outline = outline;
            this.alphaFactor = alphaFactor;
        }
    }

    public static final float PLAYER_RENDER_SCALE = 0.9375F;
    @Override
    public void scale(T entity, PoseStack matrixStack, float partialTick) { // LivingEntityRenderer#scale is public in 1.20.1
        matrixStack.scale(
                PLAYER_RENDER_SCALE * entity.getType().getDimensions().width / 0.6F, 
                PLAYER_RENDER_SCALE * entity.getType().getDimensions().height / 1.8F, 
                PLAYER_RENDER_SCALE * entity.getType().getDimensions().width / 0.6F);
    }

    private static final float OVERLAY_TICKS = 10.0F;
    @Override
    protected float getWhiteOverlayProgress(StandEntity entity, float partialTick) {
        return entity.isArmsOnlyMode() || entity.overlayTickCount > OVERLAY_TICKS ? 0 : 
            (OVERLAY_TICKS - Mth.clamp(entity.overlayTickCount + partialTick, 0.0F, OVERLAY_TICKS)) / OVERLAY_TICKS;
    }
    
    // pre-pre-render
    @Override
    public boolean shouldRender(T entity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        if (super.shouldRender(entity, pCamera, pCamX, pCamY, pCamZ)) {
            viewObstructionPrevention = obstructsView(entity, ClientUtil.getPartialTick());
            
            if (entity instanceof IEntityGlowColor) {
                IEntityGlowColor entityColor = (IEntityGlowColor) entity;
                if (viewObstructionPrevention.outline) {
                    IStandPower standPower = entity.getUserPower();
                    if (standPower != null) {
                        entityColor.setGlowColor(OptionalInt.of(ActionsOverlayGui.getPowerUiColor(standPower)));
                        entity.refreshGlowing = true;
                    }
                }
                else if (entity.refreshGlowing) {
                    entity.refreshGlowing = false;
                    entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(EntityUtilCap::refreshClEntityGlowing);
                }
            }
            
            return true;
        }
        
        return false;
    }

    public boolean firstPersonRender = false;
    private float alpha;
    private ViewObstructionPrevention viewObstructionPrevention = ViewObstructionPrevention.NONE;
    private boolean isVisibilityInverted = false;
    @Override
    public void render(T entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<T, M>(entity, this, partialTick, matrixStack, buffer, packedLight))) return;
        M model = getModel(entity);
        matrixStack.pushPose();
        model.attackTime = this.getAttackAnim(entity, partialTick);
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        model.riding = shouldSit;
        model.young = entity.isBaby();
        float yBodyRotation = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yHeadRotation = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float yRotationOffset = yHeadRotation - yBodyRotation;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity vehicle = (LivingEntity)entity.getVehicle();
            yBodyRotation = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            float f3 = Mth.clamp(Mth.wrapDegrees(yRotationOffset - yBodyRotation), -85F, 85F);
            yBodyRotation = yRotationOffset - f3;
            if (Math.abs(f3) > 50F) {
                yBodyRotation += f3 * 0.2F;
            }
            yRotationOffset = yRotationOffset - yBodyRotation;
        }
        
        float xRotation = Mth.lerp(partialTick, entity.xRotO, entity.xRot);
        float ticks = getBob(entity, partialTick);
        float walkAnimSpeed = 0.0F;
        float walkAnimPos = 0.0F;
        if (entity.isAlive()) {
            walkAnimSpeed = Mth.lerp(partialTick, entity.walkAnimation.speedOld, entity.walkAnimation.speed);
            walkAnimPos = entity.walkAnimation.position - entity.walkAnimation.speed * (1.0F - partialTick);
            if (entity.isBaby()) {
                walkAnimPos *= 3.0F;
            }

            if (walkAnimSpeed > 1.0F) {
                walkAnimSpeed = 1.0F;
            }
        }
        
        model.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
        model.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        model.getAnimator().poseStandPost(entity, model);
        entity.getBarrageSwingsHolder().updateSwings(Minecraft.getInstance());
        model.addBarrageSwings(entity);
        int packedOverlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick));

        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                matrixStack.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
            }
        }
        
        if (firstPersonRender) {
            model.setupFirstPersonRotations(matrixStack, entity, xRotation, yRotation, yBodyRotation);
        }

        setupRotations(entity, matrixStack, ticks, yBodyRotation, partialTick);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        scale(entity, matrixStack, partialTick);
        matrixStack.translate(0.0D, -1.501D, 0.0D);
        
        idlePoseSwaying(entity, ticks, matrixStack);

        if (viewObstructionPrevention != ViewObstructionPrevention.NONE) {
            entity.setNoFireAnimFrame();
        }
        ResourceLocation texture = getTextureLocation(entity);
        
        
        if (viewObstructionPrevention.outline) {
            isVisibilityInverted = true;
            model.setVisibility(entity, visibilityMode(entity).invert(isVisibilityInverted), viewObstructionPrevention.armsOnly, firstPersonRender);
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.outline(texture));
            
            model.render(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1, 1, 1, 1);
            for (RenderLayer<T, M> layerRenderer : this.layers) {
                if (layerRenderer instanceof StandModelLayerRenderer) {
                    StandModelLayerRenderer<T, M> standLayerRenderer = (StandModelLayerRenderer<T, M>) layerRenderer;
                    RenderType renderType = standLayerRenderer.getRenderType(entity, RenderType::outline);
                    standLayerRenderer.render(matrixStack, buffer, renderType, packedLight, entity, 
                            walkAnimPos, walkAnimSpeed, partialTick, ticks, yRotationOffset, xRotation);
                }
            }
        }
        isVisibilityInverted = false;
        
        
        model.setVisibility(entity, visibilityMode(entity), viewObstructionPrevention.armsOnly, firstPersonRender);
        alpha = calcAlpha(entity, partialTick);
        
        RenderType renderType = getRenderType(entity, texture);
        if (renderType != null) {
            VertexConsumer vertexBuilder = buffer.getBuffer(renderType);
            model.render(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1, 1, 1, alpha);
        }
        for (RenderLayer<T, M> layerRenderer : this.layers) {
            layerRenderer.render(matrixStack, buffer, packedLight, entity, 
                    walkAnimPos, walkAnimSpeed, partialTick, ticks, yRotationOffset, xRotation);
        }

        matrixStack.popPose();

        RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(entity, entity.getDisplayName(), this, matrixStack, buffer, packedLight, partialTick);
        MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || this.shouldShowName(entity))) {
            this.renderNameTag(entity, renderNameplateEvent.getContent(), matrixStack, buffer, packedLight);
        }

        entity.lastRenderTick = ticks;
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<T, M>(entity, this, partialTick, matrixStack, buffer, packedLight));
    }

    public void renderLayer(PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight,
            T entity, float walkAnimSpeed, float walkAnimPos, float partialTick,
            float ticks, float yRotationOffset, float xRotation, M model) {
        getModel(entity).copyPropertiesTo(model);
        model.setVisibility(entity, visibilityMode(entity).invert(isVisibilityInverted), viewObstructionPrevention.armsOnly, firstPersonRender);
        model.prepareMobModel(entity, walkAnimSpeed, walkAnimPos, partialTick);
        model.setupAnim(entity, walkAnimSpeed, walkAnimPos, ticks, yRotationOffset, xRotation);
        model.getAnimator().poseStandPost(entity, model);
        
        int packedOverlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick));
        model.render(entity, matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
    }
    
    public void renderFirstPerson(T entity, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        firstPersonRender = true;
        float yRot = Mth.lerp(partialTick, entity.yRotO, entity.yRot);
        render(entity, yRot, partialTick, matrixStack, buffer, packedLight);
        firstPersonRender = false;
    }

    private VisibilityMode visibilityMode(T entity) {
        if (!entity.isArmsOnlyMode()) {
            return VisibilityMode.ALL;
        }
        boolean mainArm = entity.showArm(InteractionHand.MAIN_HAND);
        boolean offArm = entity.showArm(InteractionHand.OFF_HAND);
        if (mainArm && offArm) {
            return VisibilityMode.ARMS_ONLY;
        }
        HumanoidArm hand = offArm ? entity.getMainArm().getOpposite() : entity.getMainArm();
        switch (hand) {
        case RIGHT:
            return VisibilityMode.RIGHT_ARM_ONLY;
        default:
            return VisibilityMode.LEFT_ARM_ONLY;
        }
    }
    
    protected void idlePoseSwaying(T entity, float ticks, PoseStack matrixStack) {
        M model = getModel(entity);
        if (model.usesGeckoAnims()) return;
        if (!entity.isVisibleForAll() && entity.getStandPose() == StandPose.IDLE && 
                model.attackTime == 0 && entity.isFollowingUser()) {
            LivingEntity user = entity.getUser();
            if (!(user != null && user.isShiftKeyDown())) {
                doIdlePoseSwaying(ticks, matrixStack);
            }
        }
    }
    
    protected void doIdlePoseSwaying(float ticks, PoseStack matrixStack) {
        float idleY = Mth.sin((ticks - getModel().idleLoopTickStamp) * 0.04F) * 0.04F;
        matrixStack.translate(0.0D, idleY, 0.0D);
    }
    
    
    @Deprecated
    public void renderFirstPersonArms(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, float partialTick) {
        if (entity.getStandPose().armsObstructView) return;
        
        getModel(entity).setVisibility(entity, VisibilityMode.ARMS_ONLY, false, false);
        renderFirstPersonArm(HumanoidArm.LEFT, matrixStack, buffer, packedLight, entity, partialTick);
        renderFirstPersonArm(HumanoidArm.RIGHT, matrixStack, buffer, packedLight, entity, partialTick);
    }

    @Deprecated
    protected void renderFirstPersonArm(HumanoidArm handSide, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, float partialTick) {
        RenderType renderType = getRenderType(entity, getTextureLocation(entity));
        if (renderType != null) {
            renderSeparateLayerArm(getModel(entity), handSide, matrixStack, buffer.getBuffer(renderType), packedLight, entity, partialTick);
            for (RenderLayer<T, M> layer : layers) {
                if (layer instanceof StandModelLayerRenderer) {
                    StandModelLayerRenderer<T, M> standLayer = (StandModelLayerRenderer<T, M>) layer;
                    if (standLayer.shouldRender(entity, entity.getStandSkin())) {
                        RenderType layerRenderType = standLayer.getRenderType(entity);
                        if (layerRenderType != null) {
                            renderSeparateLayerArm(standLayer.getLayerModel(entity), handSide, matrixStack, 
                                    buffer.getBuffer(layerRenderType), standLayer.getPackedLight(packedLight), entity, partialTick);
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    private void renderSeparateLayerArm(M model, HumanoidArm handSide, PoseStack matrixStack, 
            VertexConsumer vertexBuilder, int packedLight, T entity, float partialTick) {
        matrixStack.pushPose();
        model.attackTime = this.getAttackAnim(entity, partialTick);
        model.young = entity.isBaby();
        float walkAnimSpeed = entity.walkAnimation.position - entity.walkAnimation.speed * (1.0F - partialTick);
        if (entity.isBaby()) {
            walkAnimSpeed *= 3.0F;
        }
        float walkAnimPos = Math.min(Mth.lerp(partialTick, entity.walkAnimation.speedOld, entity.walkAnimation.speed), 1.0F);
        float ticks = getBob(entity, partialTick);
        float yBodyRotation = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yHeadRotation = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float yRotationOffset = yHeadRotation - yBodyRotation;
        matrixStack.translate(0, 0.75F, 0);
        model.prepareMobModel(entity, walkAnimSpeed, walkAnimPos, partialTick); 
        model.setupAnim(entity, walkAnimSpeed, walkAnimPos, ticks, yRotationOffset, 0);
        model.getAnimator().poseStandPost(entity, model);
        if (model.attackTime > 0 || entity.getStandPose() != StandPose.IDLE && entity.getStandPose() != StandPose.SUMMON) {
            matrixStack.translate(0, -entity.getEyeHeight(), -0.25D);
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            scale(entity, matrixStack, partialTick);
            doRenderFirstPersonArm(model, handSide, matrixStack, vertexBuilder, packedLight, entity, partialTick);
        }
        else {
            float f = handSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
            matrixStack.translate((double)(f * 0.85F), -0.9D, -1.2D);
            matrixStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
            matrixStack.translate((double)(f * -1.0F), 3.6D, 3.5D);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
            matrixStack.mulPose(Axis.XP.rotationDegrees(200.0F));
            matrixStack.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
            matrixStack.translate((double)(f * 5.6F), 0.0D, 0.0D);
            model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            model.getAnimator().poseStandPost(entity, model);
            doRenderFirstPersonArm(model, handSide, matrixStack, vertexBuilder, packedLight, entity, partialTick);
        }
        matrixStack.popPose();
    }

    @Deprecated
    protected void doRenderFirstPersonArm(M model, HumanoidArm handSide, 
            PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight, T entity, float partialTick) {
        ModelPart armModelRenderer = model.getArm(handSide);
        armModelRenderer.render(matrixStack, vertexBuilder, packedLight, 
                LivingEntityRenderer.getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick)), 1.0F, 1.0F, 1.0F, entity.getAlpha(partialTick));
    }
    
    /**
     * Used only in Stand skins UI
     */
    public void renderIdleWithSkin(PoseStack matrixStack, StandSkin standSkin, MultiBufferSource buffer, float ticks) {
        matrixStack.pushPose();
        Optional<ResourceLocation> nonDefaultSkin = standSkin.getNonDefaultLocation();
        M model = getModel(nonDefaultSkin);
        model.attackTime = 0;
        model.riding = false;
        model.young = false;
        viewObstructionPrevention = ViewObstructionPrevention.NONE;
        model.updatePartsVisibility(VisibilityMode.ALL);
        
        float yRotationOffset = 0;
        float xRotation = 0;
        float partialTick = Mth.frac(ticks);
        
        doIdlePoseSwaying(ticks, matrixStack);
        model.prepareMobModel(null, 0, 0, partialTick);
        model.poseIdleLoop(null, ticks, yRotationOffset, xRotation, HumanoidArm.RIGHT);
        
        ResourceLocation texture = getTextureLocation(nonDefaultSkin);
        RenderType renderType = model.renderType(texture);
        int packedLight = ClientUtil.MAX_MODEL_LIGHT;
        if (renderType != null) {
            VertexConsumer vertexBuilder = buffer.getBuffer(renderType);
            int packedOverlay = OverlayTexture.NO_OVERLAY;
            model.renderToBuffer(matrixStack, vertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            
            for (RenderLayer<T, M> layerRenderer : this.layers) {
                if (layerRenderer instanceof StandModelLayerRenderer) {
                    StandModelLayerRenderer<T, M> standLayer = (StandModelLayerRenderer<T, M>) layerRenderer;
                    if (standLayer.shouldRender(null, nonDefaultSkin)) {
                        M layerModel = standLayer.getLayerModel(nonDefaultSkin);
                        RenderType layerRenderType = layerModel.renderType(standLayer.getLayerTexture(nonDefaultSkin));
                        VertexConsumer layerVertexBuilder = buffer.getBuffer(layerRenderType);
                        layerModel.attackTime = 0;
                        layerModel.riding = false;
                        layerModel.young = false;
                        layerModel.updatePartsVisibility(VisibilityMode.ALL);
                        layerModel.prepareMobModel(null, 0, 0, partialTick);
                        layerModel.poseIdleLoop(null, ticks, yRotationOffset, xRotation, HumanoidArm.RIGHT);
                        layerModel.renderToBuffer(matrixStack, layerVertexBuilder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }
            }
        }

        matrixStack.popPose();
    }
}
