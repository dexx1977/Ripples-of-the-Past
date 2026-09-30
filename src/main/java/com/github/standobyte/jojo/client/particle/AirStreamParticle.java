package com.github.standobyte.jojo.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class AirStreamParticle extends TextureSheetParticle {
    private final float yRot;
    private final float xRot;

    protected AirStreamParticle(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        quadSize *= 2;
        this.yRot = (float) Mth.atan2(xSpeed, zSpeed);
        this.xRot = (float) Mth.atan2(ySpeed, Mth.sqrt(xSpeed * xSpeed + zSpeed * zSpeed));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer vertexBuilder, Camera renderInfo, float partialTick) {
        renderFromRotation(vertexBuilder, renderInfo, partialTick, yRot, xRot, true);
        renderFromRotation(vertexBuilder, renderInfo, partialTick, yRot, xRot, false);
    }
    
    private void renderFromRotation(VertexConsumer vertexBuilder, Camera renderInfo, float partialTick, float yRot, float xRot, boolean mirror) {
        Vec3 pos = renderInfo.getPosition();
        float f = (float) (Mth.lerp(partialTick, xo, x) - pos.x());
        float f1 = (float) (Mth.lerp(partialTick, yo, y) - pos.y());
        float f2 = (float) (Mth.lerp(partialTick, zo, z) - pos.z());
        Quaternionf quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.mul(Axis.YP.rotation(yRot));
        quaternion.mul(Axis.XP.rotation(-xRot));
        if (mirror) {
            quaternion.mul(Axis.YP.rotation((float) Math.PI / 2F));
        }
        else {
            quaternion.mul(Axis.YP.rotation(-(float) Math.PI / 2F));
        }
        

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.rotate(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = getQuadSize(partialTick);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float u0 = mirror ? getU1() : getU0();
        float u1 = mirror ? getU0() : getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(partialTick);
        vertexBuilder.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        vertexBuilder.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        vertexBuilder.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        vertexBuilder.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        } else {
            move(xd, yd, zd);
        }
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            AirStreamParticle particle = new AirStreamParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(spriteSet);
            particle.scale(1.5F);
            particle.setLifetime(16);
            particle.hasPhysics = false;
            return particle;
        }
    }

}
