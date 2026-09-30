package com.github.standobyte.jojo.client.render.entity.model;

import static com.github.standobyte.jojo.entity.MRDetectorEntity.DETECTION_RADIUS;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.block.BlockSprites;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.EntityModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.core.Vec3i;
import com.mojang.math.Axis;

// Made with Blockbench 3.9.2


public class MRDetectorModel extends EntityModel<MRDetectorEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart detector;
    private final Map<Direction, Float> flamesStrength;

    public MRDetectorModel() {
        texWidth = 32;
        texHeight = 32;

        detector = new ModelPart(this);
        detector.setPos(0.0F, 0.0F, 0.0F);
        detector.texOffs(0, 9).addBox(-4.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, -0.2F, false);
        detector.texOffs(0, 11).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, -0.2F, false);
        detector.texOffs(0, 0).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 8.0F, -0.2F, false);
        
        flamesStrength = new EnumMap<Direction, Float>(Direction.class);
        for (Direction direction : Direction.values()) {
            flamesStrength.put(direction, 0F);
        }
    }

    @Override
    public void setupAnim(MRDetectorEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        if (entity.isEntityDetected()) {
            Vector3f directionVec = entity.getDetectedDirection();
            for (Map.Entry<Direction, Float> entry : flamesStrength.entrySet()) {
                Direction direction = entry.getKey();
                float distance;
                switch (direction.getAxis()) {
                case X:
                    distance = directionVec.x();
                    break;
                case Y:
                    distance = directionVec.y();
                    break;
                case Z:
                    distance = directionVec.z();
                    break;
                default:
                    continue;
                }
                if (direction.getAxisDirection() == AxisDirection.POSITIVE && distance >= -1F ||
                        direction.getAxisDirection() == AxisDirection.NEGATIVE && distance <= 1F) {
                    entry.setValue(((float) DETECTION_RADIUS - (float) Math.abs(distance)) / ((float) DETECTION_RADIUS));
                }
                else {
                    entry.setValue(-1F);
                }
            }
        }
        else {
            for (Map.Entry<Direction, Float> entry : flamesStrength.entrySet()) {
                entry.setValue(-1F);
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        detector.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    
    public void renderFlames(PoseStack matrixStack, MultiBufferSource buffer, Camera camera) {
        for (Map.Entry<Direction, Float> entry : flamesStrength.entrySet()) {
            float strength = entry.getValue();
            if (strength > 0) {
                Vec3i normal = entry.getKey().getNormal();
                renderFlame(matrixStack, buffer, Vec3.atLowerCornerOf(normal).scale(0.25D), strength, camera);
            }
        }
    }
    
    private void renderFlame(PoseStack matrixStack, MultiBufferSource buffer, Vec3 offset, float strength, Camera camera) {
        TextureAtlasSprite spriteFire0 = BlockSprites.MR_FIRE_BLOCK_0.sprite();
        TextureAtlasSprite spriteFire1 = BlockSprites.MR_FIRE_BLOCK_1.sprite();
        matrixStack.pushPose();
        matrixStack.translate(offset.x, offset.y, offset.z);
        float scale = strength * 0.2F;
        matrixStack.scale(scale, scale, scale);
        float f1 = 0.5F;
        float f3 = 0.5F;
        float f4 = 0.0F;
        matrixStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        matrixStack.translate(0.0D, 0.0D, (double)(-0.3F + (float)((int)f3) * 0.02F));
        float f5 = 0.0F;
        int i = 0;
        VertexConsumer vertexBuilder = buffer.getBuffer(Sheets.translucentCullBlockSheet());

        for (PoseStack.Pose matrixstack$entry = matrixStack.last(); f3 > 0.0F; ++i) {
            TextureAtlasSprite sprite = i % 2 == 0 ? spriteFire0 : spriteFire1;
            float texU0 = sprite.getU0();
            float texV0 = sprite.getV0();
            float texU1 = sprite.getU1();
            float texV1 = sprite.getV1();
            if (i / 2 % 2 == 0) {
                float tmp = texU1;
                texU1 = texU0;
                texU0 = tmp;
            }

            ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                    ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, strength, 
                    f1 - 0.0F, 0.0F - f4, f5, texU1, texV1);
            ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                    ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, strength, 
                    -f1 - 0.0F, 0.0F - f4, f5, texU0, texV1);
            ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                    ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, strength, 
                    -f1 - 0.0F, 1.4F - f4, f5, texU0, texV0);
            ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                    ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, strength, 
                    f1 - 0.0F, 1.4F - f4, f5, texU1, texV0);
            f3 -= 0.45F;
            f4 -= 0.45F;
            f1 *= 0.9F;
            f5 += 0.03F;
        }
        
        matrixStack.popPose();
    }
}