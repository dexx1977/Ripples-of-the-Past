package com.github.standobyte.jojo.client.ui.marker;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import com.github.standobyte.jojo.client.ui.render.GuiDraw;
import net.minecraft.world.item.ItemDisplayContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import com.mojang.math.Axis;

public abstract class MarkerRenderer {
    private final ResourceLocation iconTexture;
    private final Action<?> iconAction;
    private final List<MarkerInstance> positions = new ArrayList<>();
    protected final Minecraft mc;
    protected boolean renderThroughBlocks = true;

    public static MarkerRenderer HG_BARRIER_DETECTION;
    public static MarkerRenderer CD_ANCHOR;
    public static MarkerRenderer CD_BLOOD_DROPS;
    public static MarkerRenderer GE_LIFEFORM;
    public static MarkerRenderer GE_REVERT_LIFEFORM;
    public static MarkerRenderer GE_MARKED_ITEM;
    
    public static void registerMarkers(Minecraft mc) {
        MarkerRenderer.Handler.addRenderer(HG_BARRIER_DETECTION = new HierophantGreenBarrierDetectionMarker(mc));
        MarkerRenderer.Handler.addRenderer(CD_ANCHOR = new CrazyDiamondAnchorMarker(mc));
        MarkerRenderer.Handler.addRenderer(CD_BLOOD_DROPS = new CrazyDiamondBloodHomingMarker(mc));
        MarkerRenderer.Handler.addRenderer(GE_LIFEFORM = new GoldExperienceLifeformMarker(mc));
        MarkerRenderer.Handler.addRenderer(GE_REVERT_LIFEFORM = new GoldExperienceLifeformRevertMarker(mc));
        MarkerRenderer.Handler.addRenderer(GE_MARKED_ITEM = new GoldExperienceMarkedItemMarker(mc));
    }
    
    
    @Deprecated
    /**
     * @deprecated use {@link MarkerRenderer#MarkerRenderer(ResourceLocation, Action, Minecraft)}
     */
    public MarkerRenderer(int color, ResourceLocation iconTexture, Minecraft mc) {
        this(iconTexture, mc);
    }
    
    public MarkerRenderer(ResourceLocation iconTexture, Minecraft mc) {
        this(iconTexture, null, mc);
    }
    
    public MarkerRenderer(ResourceLocation defaultIconTexture, Action<?> iconAction, Minecraft mc) {
        this.iconTexture = defaultIconTexture;
        this.iconAction = iconAction;
        this.mc = mc;
    }

    protected void render(PoseStack matrixStack, Camera camera, float partialTick) {
        if (shouldRender()) {
            positions.clear();
            updatePositions(positions, partialTick);
            if (!positions.isEmpty()) {
                matrixStack.pushPose();
                matrixStack.mulPose(camera.rotation());
                
                float[] rgb = ClientUtil.rgb(getColor());
                positions.forEach(marker -> {
                    if (renderThroughBlocks) {
                        RenderSystem.disableDepthTest();
                    } else {
                        RenderSystem.enableDepthTest();
                    }
                    renderAt(matrixStack, marker, camera, partialTick, rgb);
                });
                RenderSystem.enableDepthTest();
                
                matrixStack.popPose();
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void renderAt(PoseStack matrixStack, MarkerInstance marker, Camera camera, float partialTick, float[] rgb) {
        matrixStack.pushPose();
        Vec3 diff = marker.pos.subtract(camera.getPosition())
                .yRot(camera.getYRot() * MathUtil.DEG_TO_RAD)
                .xRot(camera.getXRot() * MathUtil.DEG_TO_RAD);
        
        double distance = diff.length();
        if (distance > 256) return;
        
        float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);
        
        matrixStack.translate(diff.x, diff.y, diff.z);
        matrixStack.scale(-scale, -scale, 1);
        matrixStack.scale(0.8f, 0.8f, 0.8f);
        
        matrixStack.pushPose();
        matrixStack.translate(-8, -28, 0);
        renderIcon(matrixStack, marker, partialTick);
        matrixStack.popPose();
        
        GuiDraw.bind(ClientUtil.ADDITIONAL_UI);
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0F);
        GuiDraw.blit(matrixStack, -16, -32, 0, 0, 32, 32, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (marker.outlined) {
            GuiDraw.blit(matrixStack, -16, -32, 32, 0, 32, 32, 256, 256);
        }

        matrixStack.pushPose();
        matrixStack.translate(-8, -28, 0);
        RenderSystem.disableDepthTest();
        renderIconOnBorder(matrixStack, marker, partialTick);
        RenderSystem.enableDepthTest();
        matrixStack.popPose();

        matrixStack.popPose();
    }
    
    protected void renderIcon(PoseStack matrixStack, MarkerInstance marker, float partialTick) {
        ResourceLocation icon = getIcon();
        if (icon != null) {
            GuiDraw.bind(icon);
            GuiDraw.blit(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
        }
    }
    
    @SuppressWarnings("deprecation")
    protected void renderItem(PoseStack matrixStack, ItemStack item, float partialTick) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        TextureManager textureManager = mc.textureManager;
        
        GuiDraw.bind(TextureAtlas.LOCATION_BLOCKS);
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        matrixStack.pushPose();
        matrixStack.translate(8, 8, 0);
        matrixStack.scale(16, 16, 0.0625f);
        matrixStack.scale(1, -1, -1);
        
        matrixStack.last().normal().identity(); 
        matrixStack.last().normal().rotation(Axis.XP.rotationDegrees(mc.gameRenderer.getMainCamera().getXRot() - 90));
        matrixStack.last().normal().rotation(Axis.YP.rotationDegrees(45));
        matrixStack.last().normal().rotation(Axis.ZP.rotationDegrees(45));

//        RenderSystem.disableDepthTest();
//        RenderSystem.disableCull();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        // FIXME the item model isn't rendered behind blocks/entities
        itemRenderer.renderStatic(item, ItemDisplayContext.GUI, 
                ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, matrixStack, buffer, mc.level, 0);
//        RenderSystem.enableDepthTest();
//        RenderSystem.enableCull();
        
        matrixStack.popPose();
        buffer.endBatch();
    }
    
    protected void renderIconOnBorder(PoseStack matrixStack, MarkerInstance marker, float partialTick) {}
    
    protected abstract boolean shouldRender();
    protected abstract void updatePositions(List<MarkerInstance> list, float partialTick);
    
    protected static void fillWithStandEffectTargets(List<MarkerInstance> list, float partialTick, 
            StandEffectType<?> standEffect, double range, Minecraft mc, boolean highlightLookedAt) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            List<StandEffectInstance> targets = StandEffectsTracker.getEffectsOfType(stand, 
                    standEffect, range).collect(Collectors.toList());
            Optional<StandEffectInstance> outlined = highlightLookedAt ? StandEffectsTracker.getTargetLookedAt(targets.stream(), mc.player) : Optional.empty();
            targets.forEach(effect -> {
                Entity target = effect.getTarget();
                if (target != null) {
                    list.add(new MarkerInstance(
                            target.getPosition(partialTick).add(0, target.getBbHeight() * 1.1, 0), 
                            highlightLookedAt && outlined.map(outlinedEffect -> effect == outlinedEffect).orElse(false),
                            Optional.of(effect)));
                }
            });
        });
    }
    
    protected int getColor() {
        return IStandPower.getStandPowerOptional(mc.player).map(stand -> StandSkinsManager.getUiColor(stand)).orElse(0xFFFFFF);
    }
    
    protected ResourceLocation getIcon() {
        if (iconAction != null) {
            return IPower.getPowerOptional(mc.player, iconAction.getPowerClassification())
                    .map(power -> getIconFromAction(iconAction, power))
                    .orElse(iconTexture);
        }
        return iconTexture;
    }
    
    private <P extends IPower<P, ?>> ResourceLocation getIconFromAction(Action<P> action, IPower<?, ?> power) {
        return action.getIconTexture((P) power);
    }

    @EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
    public static class Handler {
        private static Collection<MarkerRenderer> RENDERERS = new ArrayList<>();
        
        public static void addRenderer(MarkerRenderer markerRenderer) {
            RENDERERS.add(markerRenderer);
        }

        @SubscribeEvent
        public static void renderMarkers(RenderLevelStageEvent event) {
            // the staged event fires once per stage; the markers are drawn last, as
            // the old RenderWorldLastEvent did
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (!mc.options.hideGui) {
                RenderSystem.disableDepthTest();
                if (mc.options.graphicsMode().get() == GraphicsStatus.FABULOUS) { // it just works
                }

                PoseStack matrixStack = event.getPoseStack();
                RENDERERS.forEach(marker -> marker.render(matrixStack, mc.gameRenderer.getMainCamera(), event.getPartialTick()));
                
                RenderSystem.enableDepthTest();
            }
        }
    }
    
    
    
    protected static class MarkerInstance {
        protected Vec3 pos;
        protected boolean outlined;
        protected final Optional<StandEffectInstance> standEffect;
        
        public MarkerInstance(Vec3 pos, boolean outlined) {
            this(pos, outlined, Optional.empty());
        }
        
        public MarkerInstance(Vec3 pos, boolean outlined, Optional<StandEffectInstance> standEffect) {
            this.pos = pos;
            this.outlined = outlined;
            this.standEffect = standEffect;
        }
    }
}
