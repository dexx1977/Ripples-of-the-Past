package com.github.standobyte.jojo.util.mc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;

import com.github.standobyte.jojo.util.general.LazyCacheSupplier;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.resources.model.Material;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Timer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ClientReflection {
    private static final Field FIRST_PERSON_RENDERER_MAIN_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "field_187469_f");
    public static float getMainHandHeight(ItemInHandRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_MAIN_HAND_HEIGHT, renderer);
    }

    private static final Field FIRST_PERSON_RENDERER_O_MAIN_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "field_187470_g");
    public static float getMainHandHeightPrev(ItemInHandRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_O_MAIN_HAND_HEIGHT, renderer);
    }
    
    private static final Field FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "field_187471_h");
    public static float getOffHandHeight(ItemInHandRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT, renderer);
    }

    private static final Field FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "field_187472_i");
    public static float getOffHandHeightPrev(ItemInHandRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT, renderer);
    }

    private static final Method FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM = ObfuscationReflectionHelper.findMethod(ItemInHandRenderer.class, "func_228401_a_", 
            PoseStack.class, MultiBufferSource.class, int.class, float.class, float.class, HumanoidArm.class);
    public static void renderPlayerArm(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            float handHeight, float swingAnim, HumanoidArm handSide, ItemInHandRenderer renderer) {
        ReflectionUtil.invokeMethod(FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM, 
                renderer, matrixStack, buffer, packedLight, handHeight, swingAnim, handSide);
    }
    
    
    private static final Field MINECRAFT_PAUSE = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71445_n");
    public static void pauseClient(Minecraft minecraft) {
        ReflectionUtil.setFieldValue(MINECRAFT_PAUSE, minecraft, true);
    }

    private static final Field MINECRAFT_TIMER = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71428_T");
    public static Timer getTimer(Minecraft minecraft) {
        return ReflectionUtil.getFieldValue(MINECRAFT_TIMER, minecraft);
    }

    private static final Field TIMER_MS_PER_TICK = ObfuscationReflectionHelper.findField(Timer.class, "field_194149_e");
    public static void setMsPerTick(Timer timer, float msPerTick) {
        ReflectionUtil.setFloatFieldValue(TIMER_MS_PER_TICK, timer, msPerTick);
    }
    
    
    private static final Field MAIN_MENU_SCREEN_SPLASH = ObfuscationReflectionHelper.findField(TitleScreen.class, "field_73975_c");
    public static void setSplash(TitleScreen screen, String splash) {
        ReflectionUtil.setFieldValue(MAIN_MENU_SCREEN_SPLASH, screen, splash);
    }
    
    
    private static final Field INGAME_GUI_OVERLAY_MESSAGE_STRING = ObfuscationReflectionHelper.findField(Gui.class, "field_73838_g");
    public static Component getOverlayMessageString(Gui ingameGui) {
        return ReflectionUtil.getFieldValue(INGAME_GUI_OVERLAY_MESSAGE_STRING, ingameGui);
    }
    
    private static final Field INGAME_GUI_OVERLAY_MESSAGE_TIME = ObfuscationReflectionHelper.findField(Gui.class, "field_73845_h");
    public static void setOverlayMessageTime(Gui ingameGui, int time) {
        ReflectionUtil.setIntFieldValue(INGAME_GUI_OVERLAY_MESSAGE_TIME, ingameGui, time);
    }
    
    
    private static final Field RENDER_TYPE_BUFFER_IMPL_BUILDER = ObfuscationReflectionHelper.findField(MultiBufferSource.Impl.class, "field_228457_a_");
    public static BufferBuilder getBuilder(MultiBufferSource.Impl buffers) {
        return ReflectionUtil.getFieldValue(RENDER_TYPE_BUFFER_IMPL_BUILDER, buffers);
    }
    
    private static final Field RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS = ObfuscationReflectionHelper.findField(MultiBufferSource.Impl.class, "field_228458_b_");
    public static Map<RenderType, BufferBuilder> getFixedBuffers(MultiBufferSource.Impl buffers) {
        return ReflectionUtil.getFieldValue(RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS, buffers);
    }
    
    
    @Deprecated private static final Field MODEL_RENDERER_CUBES = ObfuscationReflectionHelper.findField(ModelPart.class, "field_78804_l");
    @Deprecated
    public static void setCubes(ModelPart modelRenderer, ObjectList<ModelPart.ModelBox> cubes) {
        ReflectionUtil.setFieldValue(MODEL_RENDERER_CUBES, modelRenderer, cubes);
    }
    
    @Deprecated
    public static void addCube(ModelPart modelRenderer, ModelPart.ModelBox cube) {
        List<ModelPart.ModelBox> cubes = ReflectionUtil.getFieldValue(MODEL_RENDERER_CUBES, modelRenderer);
        cubes.add(cube);
    }
    
    @Deprecated
    public static ObjectList<ModelPart.ModelBox> getCubes(ModelPart modelRenderer) {
        return ReflectionUtil.getFieldValue(MODEL_RENDERER_CUBES, modelRenderer);
    }
    
    
    @Deprecated private static final Field MODEL_RENDERER_CHILDREN = ObfuscationReflectionHelper.findField(ModelPart.class, "field_78805_m");
    @Deprecated
    public static ObjectList<ModelPart> getChildren(ModelPart modelRenderer) {
        return ReflectionUtil.getFieldValue(MODEL_RENDERER_CHILDREN, modelRenderer);
    }
    
    @Deprecated
    public static void setChildren(ModelPart modelRenderer, ObjectList<ModelPart> children) {
        ReflectionUtil.setFieldValue(MODEL_RENDERER_CHILDREN, modelRenderer, children);
    }
    
    private static final Method AGEABLE_MODEL_HEAD_PARTS = ObfuscationReflectionHelper.findMethod(AgeableListModel.class, "func_225602_a_");
    public static Iterable<ModelPart> getHeadParts(AgeableListModel<?> model) {
        return ReflectionUtil.invokeMethod(AGEABLE_MODEL_HEAD_PARTS, model);
    }
    
    private static final Method AGEABLE_MODEL_BODY_PARTS = ObfuscationReflectionHelper.findMethod(AgeableListModel.class, "func_225600_b_");
    public static Iterable<ModelPart> getBodyParts(AgeableListModel<?> model) {
        return ReflectionUtil.invokeMethod(AGEABLE_MODEL_BODY_PARTS, model);
    }

    private static final Method LIVING_RENDERER_SCALE = ObfuscationReflectionHelper.findMethod(LivingEntityRenderer.class, "func_225620_a_", 
            LivingEntity.class, PoseStack.class, float.class);
    public static void scale(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, PoseStack matrixStack, float partialTick) {
        ReflectionUtil.invokeMethod(LIVING_RENDERER_SCALE, renderer, entity, matrixStack, partialTick);
    }

    private static final Field ENTITY_RENDERER_SHADOW_RADIUS = ObfuscationReflectionHelper.findField(EntityRenderer.class, "field_76989_e");
    public static float getShadowRadius(EntityRenderer<?> renderer) {
        return ReflectionUtil.getFloatFieldValue(ENTITY_RENDERER_SHADOW_RADIUS, renderer);
    }

    private static final Field LIVING_RENDERER_LAYERS = ObfuscationReflectionHelper.findField(LivingEntityRenderer.class, "field_177097_h");
    public static <T extends LivingEntity, M extends EntityModel<T>> List<RenderLayer<T, M>> getLayers(LivingEntityRenderer<T, M> renderer) {
        return ReflectionUtil.getFieldValue(LIVING_RENDERER_LAYERS, renderer);
    }
    
    
    @Deprecated private static final Field MODEL_BOX_POLYGONS = ObfuscationReflectionHelper.findField(ModelPart.ModelBox.class, "field_78254_i");
    @Deprecated
    public static ModelPart.TexturedQuad[] getPolygons(ModelPart.ModelBox modelBox) {
        return ReflectionUtil.getFieldValue(MODEL_BOX_POLYGONS, modelBox);
    }
    
    @Deprecated
    public static void setPolygons(ModelPart.ModelBox modelBox, ModelPart.TexturedQuad[] polygons) {
        ReflectionUtil.setFieldValue(MODEL_BOX_POLYGONS, modelBox, polygons);
    }
    
    
    private static final Field TEXTURED_QUAD_VERTICES = ObfuscationReflectionHelper.findField(ModelPart.TexturedQuad.class, "field_78239_a");
    public static void setVertices(ModelPart.TexturedQuad quad, ModelPart.PositionTextureVertex[] vertices) {
        ReflectionUtil.setFieldValue(TEXTURED_QUAD_VERTICES, quad, vertices);
    }
    
    private static final Field TEXTURED_QUAD_NORMAL = ObfuscationReflectionHelper.findField(ModelPart.TexturedQuad.class, "field_228312_b_");
    public static void setNormal(ModelPart.TexturedQuad quad, Vector3f normal) {
        ReflectionUtil.setFieldValue(TEXTURED_QUAD_NORMAL, quad, normal);
    }
    
    
    private static final Field SOUND_EVENT_ACCESSOR_LIST = ObfuscationReflectionHelper.findField(WeighedSoundEvents.class, "field_188716_a");
    public static List<Weighted<Sound>> getSubAccessorsList(WeighedSoundEvents accessor) {
        return ReflectionUtil.getFieldValue(SOUND_EVENT_ACCESSOR_LIST, accessor);
    }
    
    
    private static final Field INGAME_MENU_SCREEN_SHOW_PAUSE_MENU = ObfuscationReflectionHelper.findField(PauseScreen.class, "field_222813_a");
    public static boolean showsPauseMenu(PauseScreen screen) {
        return ReflectionUtil.getBooleanFieldValue(INGAME_MENU_SCREEN_SHOW_PAUSE_MENU, screen);
    }
    
    
    private static final Field PARTICLE_MANAGER_SPRITE_SETS = ObfuscationReflectionHelper.findField(ParticleEngine.class, "field_215242_i");
    public static Map<ResourceLocation, ? extends SpriteSet> getSpriteSets(ParticleEngine particleManager) {
        return ReflectionUtil.getFieldValue(PARTICLE_MANAGER_SPRITE_SETS, particleManager);
    }
    
    private static final Field PARTICLE_MANAGER_TRACKING_EMITTERS = ObfuscationReflectionHelper.findField(ParticleEngine.class, "field_178933_d");
    public static Queue<TrackingEmitter> getTrackingEmitters(ParticleEngine particleManager) {
        return ReflectionUtil.getFieldValue(PARTICLE_MANAGER_TRACKING_EMITTERS, particleManager);
    }
    
    
    private static final Field MODEL_BAKERY_UNREFERENCED_TEXTURES = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177602_b");
    public static Set<Material> getModelBakeryUnreferencedTextures() {
        return ReflectionUtil.getFieldValue(MODEL_BAKERY_UNREFERENCED_TEXTURES, null);
    }
    
    private static final Field MINECRAFT_MOUSE_HANDLER = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71417_B");
    public static void setMouseHandler(Minecraft mc, MouseHandler mouseHandler) {
        ReflectionUtil.setFieldValue(MINECRAFT_MOUSE_HANDLER, mc, mouseHandler);
    }
    
    
    private static final Field MOUSE_HELPER_X_POS = ObfuscationReflectionHelper.findField(MouseHandler.class, "field_198040_e");
    public static void setXPos(MouseHandler mouseHelper, double xPos) {
        ReflectionUtil.setFieldValue(MOUSE_HELPER_X_POS, mouseHelper, xPos);
    }
    
    private static final Field MOUSE_HELPER_Y_POS = ObfuscationReflectionHelper.findField(MouseHandler.class, "field_198041_f");
    public static void setYPos(MouseHandler mouseHelper, double yPos) {
        ReflectionUtil.setFieldValue(MOUSE_HELPER_Y_POS, mouseHelper, yPos);
    }
    
    
    private static final Field SHADER_GROUP_PASSES = ObfuscationReflectionHelper.findField(PostChain.class, "field_148031_d");
    public static List<PostPass> getShaderGroupPasses(PostChain shaderGroup) {
        return ReflectionUtil.getFieldValue(SHADER_GROUP_PASSES, shaderGroup);
    }
    
    private static final Field GAME_RENDERER_POST_EFFECT = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_147707_d");
    public static void setPostEffect(GameRenderer gameRenderer, PostChain postEffect) {
        ReflectionUtil.setFieldValue(GAME_RENDERER_POST_EFFECT, gameRenderer, postEffect);
    }
    
    private static final Field GAME_RENDERER_EFFECT_ACTIVE = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_175083_ad");
    public static void setEffectActive(GameRenderer gameRenderer, boolean effectActive) {
        ReflectionUtil.setBooleanFieldValue(GAME_RENDERER_EFFECT_ACTIVE, gameRenderer, effectActive);
    }
    
    private static final Field GAME_RENDERER_EFFECT_INDEX = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_147713_ae");
    public static void setEffectIndex(GameRenderer gameRenderer, int effectIndex) {
        ReflectionUtil.setIntFieldValue(GAME_RENDERER_EFFECT_INDEX, gameRenderer, effectIndex);
    }
    
    
    private static final Field CLIENT_PLAYER_ENTITY_HANDS_BUSY = ObfuscationReflectionHelper.findField(LocalPlayer.class, "field_184844_co");
    public static void setHandsBusy(LocalPlayer player, boolean handsBusy) {
        ReflectionUtil.setBooleanFieldValue(CLIENT_PLAYER_ENTITY_HANDS_BUSY, player, handsBusy);
    }
    
    
    private static final Field CLIENT_PLAYER_ENTITY_FLASH_ON_SET_HEALTH = ObfuscationReflectionHelper.findField(LocalPlayer.class, "field_175169_bQ");
    public static void setFlashOnSetHealth(Player player, boolean flashOnSetHealth) {
        ReflectionUtil.setBooleanFieldValue(CLIENT_PLAYER_ENTITY_FLASH_ON_SET_HEALTH, player, flashOnSetHealth);
    }
    
    
    private static final Field KEY_BINDING_IS_DOWN = ObfuscationReflectionHelper.findField(KeyMapping.class, "field_74513_e");
    /*
     * Doesn't check the conflict context and Shift/Ctrl/... modifiers
     */
    public static boolean isDownFieldOnly(KeyMapping key) {
        return ReflectionUtil.getBooleanFieldValue(KEY_BINDING_IS_DOWN, key);
    }

    private static final Field KEY_BINDING_ALL_MAP = ObfuscationReflectionHelper.findField(KeyMapping.class, "field_74516_a");
    private static final LazyCacheSupplier<Map<String, KeyMapping>> keyBindingsMapSupplier = new LazyCacheSupplier<>(
            () -> ReflectionUtil.getFieldValue(KEY_BINDING_ALL_MAP, null));
    public static Map<String, KeyMapping> getKeyBindingsMap() {
        return keyBindingsMapSupplier.get();
    }

    private static final Field KEY_BINDING_CLICK_COUNT = ObfuscationReflectionHelper.findField(KeyMapping.class, "field_151474_i");
    public static int getClickCount(KeyMapping key) {
        return ReflectionUtil.getIntFieldValue(KEY_BINDING_CLICK_COUNT, key);
    }
    
    public static void setClickCount(KeyMapping key, int clickCount) {
        ReflectionUtil.setIntFieldValue(KEY_BINDING_CLICK_COUNT, key, clickCount);
    }
    
    private static final Field KEY_BINDING_ALL_FIELD = ObfuscationReflectionHelper.findField(KeyMapping.class, "field_74516_a");
    private static Map<String, KeyMapping> KEY_BINDINGS_ALL;
    public static Map<String, KeyMapping> getAllKeybindingMap() {
        if (KEY_BINDINGS_ALL == null) {
            KEY_BINDINGS_ALL = ReflectionUtil.getFieldValue(KEY_BINDING_ALL_FIELD, null);
        }
        return KEY_BINDINGS_ALL;
    }
    
    
    private static final Field CONTROLS_SCREEN_CONTROL_LIST = ObfuscationReflectionHelper.findField(ControlsScreen.class, "field_146494_r");
    public static KeyBindsList getControlList(ControlsScreen screen) {
        return ReflectionUtil.getFieldValue(CONTROLS_SCREEN_CONTROL_LIST, screen);
    }
    
    private static final Field KEY_BINDING_LIST_MAX_NAME_WIDTH = ObfuscationReflectionHelper.findField(KeyBindsList.class, "field_148188_n");
    public static int getMaxNameWidth(KeyBindsList keyBindingList) {
        return ReflectionUtil.getIntFieldValue(KEY_BINDING_LIST_MAX_NAME_WIDTH, keyBindingList);
    }
    
    private static final Field KEY_BINDING_LIST_KEY_ENTRY_CHANGE_BUTTON = ObfuscationReflectionHelper.findField(KeyBindsList.KeyEntry.class, "field_148280_d");
    public static Button getChangeButton(KeyBindsList.KeyEntry keyEntry) {
        return ReflectionUtil.getFieldValue(KEY_BINDING_LIST_KEY_ENTRY_CHANGE_BUTTON, keyEntry);
    }
    
    private static final Field KEY_BINDING_LIST_KEY_ENTRY_KEY = ObfuscationReflectionHelper.findField(KeyBindsList.KeyEntry.class, "field_148282_b");
    public static KeyMapping getKey(KeyBindsList.KeyEntry keyEntry) {
        return ReflectionUtil.getFieldValue(KEY_BINDING_LIST_KEY_ENTRY_KEY, keyEntry);
    }
    
    private static final Field KEY_BINDING_LIST_CATEGORY_ENTRY_NAME = ObfuscationReflectionHelper.findField(KeyBindsList.CategoryEntry.class, "field_148285_b");
    public static Component getName(KeyBindsList.CategoryEntry categoryEntry) {
        return ReflectionUtil.getFieldValue(KEY_BINDING_LIST_CATEGORY_ENTRY_NAME, categoryEntry);
    }
    
    private static final Method ABSTRACT_LIST_GET_ROW_TOP = ObfuscationReflectionHelper.findMethod(net.minecraft.client.gui.widget.list.AbstractList.class, "func_230962_i_", int.class);
    public static int getRowTop(net.minecraft.client.gui.widget.list.AbstractList<?> uiList, int rowIndex) {
        return ReflectionUtil.invokeMethod(ABSTRACT_LIST_GET_ROW_TOP, uiList, rowIndex);
    }
    
    
    private static final Field MINECRAFT_PAUSE_PARTIAL_TICK = ObfuscationReflectionHelper.findField(Minecraft.class, "field_193996_ah");
    public static float getPausePartialTick(Minecraft mc) {
        return ReflectionUtil.getFloatFieldValue(MINECRAFT_PAUSE_PARTIAL_TICK, mc);
    }
    
    private static final Field MINECRAFT_MAIN_RENDER_TARGET = ObfuscationReflectionHelper.findField(Minecraft.class, "field_147124_at");
    public static void setMainRenderTarget(Minecraft mc, RenderTarget buffer) {
        ReflectionUtil.setFieldValue(MINECRAFT_MAIN_RENDER_TARGET, mc, buffer);
    }
    
    private static final Method ACTIVE_RENDER_INFO_SET_POSITION = ObfuscationReflectionHelper.findMethod(Camera.class, "func_216774_a", Vec3.class);
    public static void setPosition(Camera camera, Vec3 position) {
        ReflectionUtil.invokeMethod(ACTIVE_RENDER_INFO_SET_POSITION, camera, position);
    }
    
    private static final Field ACTIVE_RENDER_INFO_DETACHED = ObfuscationReflectionHelper.findField(Camera.class, "field_216799_k");
    public static void setIsDetached(Camera camera, boolean detached) {
        ReflectionUtil.setBooleanFieldValue(ACTIVE_RENDER_INFO_DETACHED, camera, detached);
    }
    
    private static final Field ACTIVE_RENDER_INFO_MIRROR = ObfuscationReflectionHelper.findField(Camera.class, "field_216800_l");
    public static void setMirror(Camera camera, boolean mirror) {
        ReflectionUtil.setBooleanFieldValue(ACTIVE_RENDER_INFO_MIRROR, camera, mirror);
    }
    
    
    private static final Field NATIVE_IMAGE_PIXELS = ObfuscationReflectionHelper.findField(NativeImage.class, "field_195722_d");
    public static long getPixelsAddress(NativeImage image) {
        return ReflectionUtil.getLongFieldValue(NATIVE_IMAGE_PIXELS, image);
    }
    

    private static final Field SOUND_SOURCE_SOURCE = ObfuscationReflectionHelper.findField(Channel.class, "field_216441_b");
    public static int getSourceId(Channel source) {
        return ReflectionUtil.getIntFieldValue(SOUND_SOURCE_SOURCE, source);
    }

    private static final Method AUDIO_STREAM_BUFFER_GET_AL_BUFFER = ObfuscationReflectionHelper.findMethod(SoundBuffer.class, "func_216473_a");
    public static OptionalInt getAlBuffer(SoundBuffer buffer) {
        return ReflectionUtil.invokeMethod(AUDIO_STREAM_BUFFER_GET_AL_BUFFER, buffer);
    }

    private static final Field SOUND_ENGINE_SOUND_BUFFERS = ObfuscationReflectionHelper.findField(SoundEngine.class, "field_217939_i");
    public static SoundBufferLibrary getSoundBuffers(SoundEngine soundEngine) {
        return ReflectionUtil.getFieldValue(SOUND_ENGINE_SOUND_BUFFERS, soundEngine);
    }
    
    
    private static final Field ITEM_OVERRIDE_LIST_OVERRIDES = ObfuscationReflectionHelper.findField(ItemOverrides.class, "field_188023_b");
    public static List<ItemOverride> getOverrides(ItemOverrides itemOverrideList) {
        return ReflectionUtil.getFieldValue(ITEM_OVERRIDE_LIST_OVERRIDES, itemOverrideList);
    }
    
    private static final Field ITEM_OVERRIDE_LIST_OVERRIDE_MODELS = ObfuscationReflectionHelper.findField(ItemOverrides.class, "field_209582_c");
    public static List<BakedModel> getOverrideModels(ItemOverrides itemOverrideList) {
        return ReflectionUtil.getFieldValue(ITEM_OVERRIDE_LIST_OVERRIDE_MODELS, itemOverrideList);
    }
}
