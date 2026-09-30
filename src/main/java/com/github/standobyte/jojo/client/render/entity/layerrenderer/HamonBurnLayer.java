package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class HamonBurnLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> implements IFirstPersonHandLayer {
    
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    public HamonBurnLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        if (!entity.isInvisible()) {
            M model = getParentModel();
            ResourceLocation texture = getTexture(model, entity);
            if (texture == null) return;
            
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    @Nullable
    private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
        MobEffectInstance hamonSpread = entity.getEffect(ModStatusEffects.HAMON_SPREAD.get());
        if (hamonSpread != null) {
            int lvl = Math.min(hamonSpread.getAmplifier(), 3);
            TextureSize size = TextureSize.getClosestTexSize(model);
            return LAYER_TEXTURES.get(size)[lvl];
        }
        return null;
    }
    
    @Override
    public void renderHandFirstPerson(HumanoidArm side, PoseStack matrixStack, 
            MultiBufferSource buffer, int light, AbstractClientPlayer player, 
            PlayerRenderer playerRenderer) {
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        IFirstPersonHandLayer.defaultRender(side, matrixStack, buffer, light, player, playerRenderer, 
                model, getTexture(model, player));
    }


    private static final Map<TextureSize, ResourceLocation[]> LAYER_TEXTURES = Util.make(new EnumMap<>(TextureSize.class), map -> {
        map.put(TextureSize._64x32, new ResourceLocation[] {
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x32/1.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x32/2.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x32/3.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x32/4.png")
        });
        map.put(TextureSize._64x64, new ResourceLocation[] {
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x64/1.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x64/2.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x64/3.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t64x64/4.png")
        });
        map.put(TextureSize._128x64, new ResourceLocation[] {
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x64/1.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x64/2.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x64/3.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x64/4.png")
        });
        map.put(TextureSize._128x128, new ResourceLocation[] {
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x128/1.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x128/2.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x128/3.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t128x128/4.png")
        });
        map.put(TextureSize._256x128, new ResourceLocation[] {
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x128/1.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x128/2.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x128/3.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x128/4.png")
        });
        map.put(TextureSize._256x256, new ResourceLocation[] {
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x256/1.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x256/2.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x256/3.png"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/hamon_burn/t256x256/4.png")
        });
    });
    
    public static enum TextureSize {
        _64x32(6, 5),
        _64x64(6, 6),
        _128x64(7, 6),
        _128x128(7, 7),
        _256x128(8, 7),
        _256x256(8, 8);
        
//        private final int widthLog2;
//        private final int heightLog2;
        
        private TextureSize(int widthLog2, int heightLog2) {
//            this.widthLog2 = widthLog2;
//            this.heightLog2 = heightLog2;
        }
        
        public static TextureSize getClosestTexSize(Model model) {
            int widthLog = Mth.ceillog2(ModelPart.textureWidthOf(model));
            int heightLog = Mth.ceillog2(ModelPart.textureHeightOf(model));
            
            widthLog = Mth.clamp(widthLog, 6, 8);
            heightLog = Mth.clamp(heightLog, widthLog - 1, widthLog);
            int i = (widthLog - 6) * 2;
            if (heightLog == widthLog) i++;
            
            return TextureSize.values()[i];
        }
    }
    
}
