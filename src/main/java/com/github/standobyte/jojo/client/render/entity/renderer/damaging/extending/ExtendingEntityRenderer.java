package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.RepeatingModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class ExtendingEntityRenderer<T extends OwnerBoundProjectileEntity, M extends RepeatingModel<T>> extends SimpleEntityRenderer<T, M> {

    public ExtendingEntityRenderer(EntityRendererProvider.Context context, M model, ResourceLocation texPath) {
        super(context, model, texPath);
    }
    
    protected float getAlpha(T entity, float partialTick) {
        if (entity.standDamage()) {
            LivingEntity owner = entity.getOwner();
            if (owner instanceof StandEntity) {
                return ((StandEntity) owner).getAlpha(partialTick);
            }
        }
        return 1.0F;
    }

    @Override
    protected void rotateModel(M model, T entity, float partialTick, float yRotation, float xRotation, PoseStack matrixStack) {
        Vec3 originPos = getOriginPos(entity, partialTick);
        Vec3 entityPos = new Vec3(
                Mth.lerp((double) partialTick, entity.xo, entity.getX()), 
                Mth.lerp((double) partialTick, entity.yo, entity.getY()), 
                Mth.lerp((double) partialTick, entity.zo, entity.getZ()));
        Vec3 extentVec = entityPos.subtract(originPos);
        if (entity.tickCount % 20 == 0) {
            System.out.println("[DBG-EXT] id=" + entity.getId() + " origin=" + originPos + " pos=" + entityPos + " len=" + extentVec.length() + " invisible=" + entity.isInvisible() + " bound=" + entity.isBoundToOwner());
        }
        yRotation = MathUtil.yRotDegFromVec(extentVec);
        xRotation = MathUtil.xRotDegFromVec(extentVec);
        model.setLength((float) extentVec.length());
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    protected Vec3 getOriginPos(T entity, float partialTick) {
        return entity.getOriginPoint(partialTick);
    }
    
    @Override
    protected void doRender(T entity, M model, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            packedLight = entityRenderDispatcher.getPackedLightCoords(entity.getOwner(), partialTick);
        }
        super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
    }
    
    @Override
    protected void renderModel(T entity, M model, float partialTick, PoseStack matrixStack, VertexConsumer vertexBuilder, int packedLight) {
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, getAlpha(entity, partialTick));
    }
}
