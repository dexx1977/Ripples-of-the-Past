package com.github.standobyte.jojo.client.render.world.shader;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientTimeStopHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;

public class ShaderEffectApplier {
    private static final ShaderResLocation DUMMY = new ShaderResLocation(new ResourceLocation("dummy", "dummy"), false);
    private static ShaderEffectApplier instance;
    
    private final Minecraft mc;
    private boolean resetShader;
    
    private Random random = new Random();
    private ShaderResLocation resolveShader = null;
    
    private ShaderEffectApplier(Minecraft mc) {
        this.mc = mc;
    }
    
    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ShaderEffectApplier(mc);
        }
    }
    
    public static ShaderEffectApplier getInstance() {
        return instance;
    }
    
    
    public void setResetShader() {
        this.resetShader = true;
    }
    
    public void shaderTick() {
        if (resetShader) {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
            resetShader = false;
        }

        if (mc.gameRenderer.currentEffect() == null) {
            updateCurrentShader();
        }
    }
    
    public void updateCurrentShader() {
        ShaderResLocation shader = getCurrentShader();
        if (shader != null && shader != DUMMY) {
            loadShader(shader);
        }
    }
    
    @Nullable
    private ShaderResLocation getCurrentShader() {
        if (mc.level == null) {
            return null;
        }
        
        ClientTimeStopHandler tsFields = ClientTimeStopHandler.getInstance();
        if (tsFields.isTimeStopped() && tsFields.canSeeInStoppedTime() && timeStopAction != null) {
            boolean animationConfig = ClientModSettings.getSettingsReadOnly().timeStopAnimation;
            ResourceLocation timeStopShader = timeStopAction.getTimeStopShader(animationConfig);
            if (timeStopShader != null) {
                return ShaderResLocation.fromResLoc(timeStopShader, animationConfig);
            }
        }
        tsShaderStarted = false;
        
        if (ClientModSettings.getSettingsReadOnly().resolveShaders && resolveShader != null) {
            return resolveShader;
        }
        return null;
    }
    
    private void loadShader(ShaderResLocation shader) {
        if (shader.hasCustomParam) {
            loadCustomParametersEffect(mc.gameRenderer, mc, shader.resLoc);
        }
        else {
            mc.gameRenderer.loadEffect(shader.resLoc);
        }
    }
    
    private static void loadCustomParametersEffect(GameRenderer gameRenderer, Minecraft minecraft, ResourceLocation name) {
        if (gameRenderer.currentEffect() != null) {
            gameRenderer.currentEffect().close();
        }

        try {
            PostChain effect = new CustomShaderGroup(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), name);
            ClientReflection.setPostEffect(gameRenderer, effect);
            effect.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            ClientReflection.setEffectActive(gameRenderer, true);
        } catch (IOException ioexception) {
            JojoMod.getLogger().warn("Failed to load shader: {}", name, ioexception);
            ClientReflection.setEffectIndex(gameRenderer, GameRenderer.EFFECT_NONE);
            ClientReflection.setEffectActive(gameRenderer, false);
        } catch (JsonSyntaxException jsonsyntaxexception) {
            JojoMod.getLogger().warn("Failed to parse shader: {}", name, jsonsyntaxexception);
            ClientReflection.setEffectIndex(gameRenderer, GameRenderer.EFFECT_NONE);
            ClientReflection.setEffectActive(gameRenderer, false);
        }
    }

    
    public void setRandomResolveShader() {
        if (resolveShader != null) return;
        
        resolveShader = ShaderResLocation.fromResLoc(CustomResources.getResolveShadersListManager()
                .getRandomShader(IStandPower.getPlayerStandPower(mc.player), random), 
                false);
        if (resolveShader == null) {
            resolveShader = DUMMY;
        }
        else {
            try {
                @SuppressWarnings("unused")
                PostChain tryLoadShader = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), resolveShader.resLoc);
            } catch (JsonSyntaxException e) {
                JojoMod.getLogger().warn("Failed to load shader: {}", resolveShader, e);
                resolveShader = DUMMY;
            } catch (IOException e) {
                JojoMod.getLogger().warn("Failed to parse shader: {}", resolveShader, e);
                resolveShader = DUMMY;
            }
        }
    }

    public void stopResolveShader() {
        if (resolveShader != null) {
            resetShader = true;
            resolveShader = null;
        }
    }
    
    
    public void addTsShaderUniforms(EffectInstance tsShader, float partialSecond, float tsEffectLength) {
        if (ClientTimeStopHandler.getInstance().isTimeStopped()) {
            ClientTimeStopHandler tsFields = ClientTimeStopHandler.getInstance();
            float partialTick = Mth.frac(partialSecond * 20F);
            float tsTick = tsFields.getTimeStopTicks() + partialTick;
            tsShader.safeGetUniform("TSTicks") .set(tsTick);
            tsShader.safeGetUniform("TSLength").set((float) tsFields.getTimeStopLength());
            if (!ClientModSettings.getSettingsReadOnly().timeStopAnimation || tsPosOnScreen == null) {
                tsShader.safeGetUniform("TSEffectLength").set(0F);
            }
            if (tsPosOnScreen != null) {
                tsShader.safeGetUniform("CenterScreenCoord").set(new float[] {tsPosOnScreen.pos.x, tsPosOnScreen.pos.y});
            }
        }
        else {
            tsShader.safeGetUniform("TSTicks") .set(0F);
            tsShader.safeGetUniform("TSLength").set(-1F);
        }
    }
    
    private boolean tsShaderStarted;
    private Entity timeStopper;
    private TimeStop timeStopAction;
    @Nullable private ClientUtil.PosOnScreen tsPosOnScreen;
    public void setTimeStopVisuals(Entity timeStopper, TimeStop action) {
        if (!tsShaderStarted) {
            this.timeStopper = timeStopper;
            this.timeStopAction = action;
            tsShaderStarted = true;
        }
    }
    
    public void updateTimeStopperScreenPos(PoseStack matrixStack, Matrix4f projection, Camera camera, float partialTick) {
        if (ClientTimeStopHandler.getInstance().isTimeStopped()) {
            if (timeStopper == mc.player) {
                tsPosOnScreen = ClientUtil.PosOnScreen.SCREEN_CENTER;
            }
            else if (timeStopper != null) {
                Vec3 entityPos = timeStopper.getPosition(partialTick).add(0, timeStopper.getBbHeight() * 0.5f, 0);
                tsPosOnScreen = ClientUtil.posOnScreen(entityPos, camera, matrixStack, projection);
                if (tsShaderStarted) {
                    if (tsPosOnScreen == null || !tsPosOnScreen.isOnScreen) {
                        tsPosOnScreen = null;
                        timeStopper = null;
                    }
                    tsShaderStarted = false;
                }
            }
            else {
                tsPosOnScreen = null;
            }
        }
    }
    
    
    static class ShaderResLocation {
        public final ResourceLocation resLoc;
        public final boolean hasCustomParam;
        
        private ShaderResLocation(@Nonnull ResourceLocation resLoc, boolean hasCustomParam) {
            Objects.nonNull(resLoc);
            this.resLoc = resLoc;
            this.hasCustomParam = hasCustomParam;
        }
        
        @Nullable
        public static ShaderResLocation fromResLoc(@Nullable ResourceLocation resLoc, boolean hasCustomParam) {
            return resLoc != null ? new ShaderResLocation(resLoc, hasCustomParam) : null;
        }
    }
}
