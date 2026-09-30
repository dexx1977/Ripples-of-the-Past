package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.mob.CocoJumboTurtleModel;
import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class CocoJumboTurtleRenderer extends MobRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {
    private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");
    private static final ResourceLocation TURTLE_LOCATION_2 = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/mob/turtle_extra.png");
    private static final ResourceLocation KEY_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/mob/turtle_key.png");
    
    public CocoJumboTurtleRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new CocoJumboTurtleModel<>(0.0F), 0.7F);
        addLayer(new CocoJumboExtraTextureStuff(this));
        addLayer(new MrPresidentKeyLayer(this));
    }
    
    @Override
    public void render(CocoJumboTurtleEntity entity, float yRot, float partialTick, 
            PoseStack matrixStack, MultiBufferSource buffer, int light) {
        if (entity.isBaby()) {
            this.shadowRadius *= 0.5F;
        }
        model.hasKey = entity.hasKey();
        matrixStack.pushPose();
        
        LivingEntity carrier = entity.getCarrier();
        if (carrier != null) {
            Minecraft mc = Minecraft.getInstance();
            if (carrier == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) {
                Vec3 realOffset = CocoJumboTurtleEntity.carryOffset(
                        Mth.lerp(partialTick, carrier.yBodyRotO, carrier.yBodyRot), carrier);
                Vec3 headRotOffset = CocoJumboTurtleEntity.carryOffset(
                        Mth.lerp(partialTick, carrier.yRotO, carrier.yRot), carrier);
                Vec3 offset = headRotOffset.subtract(realOffset);
                matrixStack.translate(offset.x, carrier.getBbHeight() * 0.2, offset.z);
            }
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            matrixStack.mulPose(Axis.YP.rotationDegrees(carrier.getMainArm() == HumanoidArm.RIGHT ? -60 : 60));
            shadowRadius = 0;
        }

        super.render(entity, yRot, partialTick, matrixStack, buffer, light);
        matrixStack.popPose();
    }
    
    @Override
    public ResourceLocation getTextureLocation(CocoJumboTurtleEntity pEntity) {
        return TURTLE_LOCATION;
    }
    
    
    
    public static class CocoJumboExtraTextureStuff extends RenderLayer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {

        public CocoJumboExtraTextureStuff(RenderLayerParent<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight,
                CocoJumboTurtleEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks,
                float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(TURTLE_LOCATION_2));
            this.getParentModel().renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        
    }
    
    public static class MrPresidentKeyLayer extends RenderLayer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {

        public MrPresidentKeyLayer(RenderLayerParent<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight,
                CocoJumboTurtleEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks,
                float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (pLivingEntity.hasKey()) {
                VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(KEY_LOCATION));
                this.getParentModel().renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        
    }
}
