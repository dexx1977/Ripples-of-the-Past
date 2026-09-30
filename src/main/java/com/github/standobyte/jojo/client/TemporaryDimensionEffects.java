package com.github.standobyte.jojo.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TemporaryDimensionEffects {
    /*
     * 1.16.5 stored sky/cloud/weather renderers on the dimension effects object
     * through Forge interfaces. Those interfaces are gone and 1.20.1's
     * DimensionSpecialEffects is a plain data holder (the sky, cloud and weather
     * drawing lives in LevelRenderer and is hooked through RenderLevelStageEvent),
     * so the handlers are the mod's own types here. Note that the effects object
     * itself can no longer be mutated - applying a stored set of handlers needs the
     * level renderer stages; this class only keeps the stack of them.
     */
    public interface IWeatherRenderHandler {
        void render(int ticks, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, ClientLevel level,
                Minecraft mc, com.mojang.blaze3d.vertex.VertexConsumer buffer, double camX, double camY, double camZ);
    }

    public interface IWeatherParticleRenderHandler {
        void render(int ticks, ClientLevel level, Minecraft mc, double camX, double camY, double camZ);
    }

    public interface ISkyRenderHandler {
        void render(int ticks, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, ClientLevel level,
                Minecraft mc, org.joml.Matrix4f projectionMatrix);
    }

    public interface ICloudRenderHandler {
        void render(int ticks, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, ClientLevel level,
                Minecraft mc, org.joml.Matrix4f projectionMatrix, double camX, double camY, double camZ);
    }

    private static TemporaryDimensionEffects instance = new TemporaryDimensionEffects();
    
    public static void init() {
        MinecraftForge.EVENT_BUS.register(instance);
    }
    
    public static TemporaryDimensionEffects getInstance() {
        return instance;
    }
    
    
    private Map<ResourceLocation, DimensionEffectsStack> effectsByDimension = new HashMap<>();
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (effectsByDimension.isEmpty() || event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() == null) {
            return;
        }
        
        ResourceLocation key = mc.level.dimension().location();
        DimensionEffectsStack stack = effectsByDimension.get(key);
        if (stack == null) {
            return;
        }
        
        if (stack.tick(mc.level)) {
            effectsByDimension.remove(key);
        }
    }
    
    public DimensionEffectsStack getEffectsStack(ClientLevel world) {
        DimensionEffectsStack stack = effectsByDimension.computeIfAbsent(
                world.dimension().location(), __ -> new DimensionEffectsStack(world));
        return stack;
    }
    
    
    
    public static class DimensionEffectsStack {
        private DimensionEffect prevOtherEffects;
        private Stack<DimensionEffect> temporaryEffectsStack = new Stack<>();
        
        private DimensionEffectsStack(ClientLevel world) {
            this.prevOtherEffects = new DimensionEffect();
            // 1.20.1 has no handlers on the effects object to remember

        }
        
        public boolean addEffect(ClientLevel world, DimensionEffect effect) {
            if (temporaryEffectsStack.isEmpty() || temporaryEffectsStack.peek() != effect) {
                temporaryEffectsStack.add(effect);
                effect.isActive = true;
                return true;
            }
            return false;
        }
        
        public boolean addEffectLast(ClientLevel world, DimensionEffect effect) {
            if (temporaryEffectsStack.isEmpty() || temporaryEffectsStack.firstElement() != effect) {
                temporaryEffectsStack.insertElementAt(effect, 0);
                effect.isActive = true;
                return true;
            }
            return false;
        }
        
        private boolean tick(ClientLevel world) {
            Iterator<DimensionEffect> iter = temporaryEffectsStack.iterator();
            boolean updateEffects = false;
            while (iter.hasNext()) {
                DimensionEffect effects = iter.next();
                if (effects.isActive()) {
                    break;
                }
                else {
                    iter.remove();
                    updateEffects = true;
                }
            }
            
            if (updateEffects) {
                DimensionEffect effects = temporaryEffectsStack.isEmpty() ? prevOtherEffects : temporaryEffectsStack.peek();
            }
            
            return temporaryEffectsStack.isEmpty();
        }
    }
    
    
    
    public static class DimensionEffect {
        private IWeatherRenderHandler weatherRenderer;
        private IWeatherParticleRenderHandler weatherParticleRenderer;
        private ISkyRenderHandler skyRenderer;
        private ICloudRenderHandler cloudRenderer;
        
        private boolean isActive;
        
        public DimensionEffect withWeatherRenderer(IWeatherRenderHandler weatherRenderer) {
            this.weatherRenderer = weatherRenderer;
            return this;
        }
        
        public DimensionEffect withWeatherParticleRenderer(IWeatherParticleRenderHandler weatherParticleRenderer) {
            this.weatherParticleRenderer = weatherParticleRenderer;
            return this;
        }
        
        public DimensionEffect withSkyRenderer(ISkyRenderHandler skyRenderer) {
            this.skyRenderer = skyRenderer;
            return this;
        }
        
        public DimensionEffect withCloudRenderer(ICloudRenderHandler cloudRenderer) {
            this.cloudRenderer = cloudRenderer;
            return this;
        }
        
        /** Keeps the handlers that were active before this effect was pushed. */
        public void saveEffects(DimensionEffect current) {
            if (current == null) {
                return;
            }
            this
            .withWeatherRenderer(current.weatherRenderer)
            .withWeatherParticleRenderer(current.weatherParticleRenderer)
            .withSkyRenderer(current.skyRenderer)
            .withCloudRenderer(current.cloudRenderer);
        }
        
        /** Makes this effect the active one (see the note on the class). */
        public DimensionEffect setTo(DimensionEffect previous) {
            return this;
        }
        
        public void setActive(boolean active) {
            this.isActive = active;
        }
        
        public boolean isActive() {
            return isActive;
        }
        
        public DimensionEffect copy() {
            DimensionEffect effects = new DimensionEffect();
            effects.weatherRenderer = this.weatherRenderer;
            effects.weatherParticleRenderer = this.weatherParticleRenderer;
            effects.skyRenderer = this.skyRenderer;
            effects.cloudRenderer = this.cloudRenderer;
            return effects;
        }
    }
    
}
