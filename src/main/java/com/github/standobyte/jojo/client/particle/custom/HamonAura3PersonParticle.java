package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.particle.HamonAuraParticle;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HamonAura3PersonParticle extends HamonAuraParticle {
    private final LivingEntity user;
    private final AbstractClientPlayer userAsPlayer;
    private Vec3 userPositionPrev;
    
    protected HamonAura3PersonParticle(ClientLevel world, LivingEntity entity, 
            double x, double y, double z, double xda, double yda, double zda,
            SpriteSet sprites) {
        super(world, x, y, z, xda, yda, zda, sprites);
        this.user = entity;
        this.userPositionPrev = entity.position();
        this.userAsPlayer = user instanceof AbstractClientPlayer ? (AbstractClientPlayer) user : null;
    }
    
    @Override
    public void render(VertexConsumer vertexBuilder, Camera camera, float partialTick) {
        if (!ClientModSettings.getSettingsReadOnly().thirdPersonHamonAura) return;
        
        if (user != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.cameraEntity == user && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                return;
            }
        }
        
        Vec3 playerAnimPos = userAsPlayer != null ? PlayerAnimationHandler.getPlayerAnimator().getBodyPos(userAsPlayer, partialTick) : Vec3.ZERO;
        x += playerAnimPos.x;
        y += playerAnimPos.y;
        z += playerAnimPos.z;
        xo += playerAnimPos.x;
        yo += playerAnimPos.y;
        zo += playerAnimPos.z;
        
        super.render(vertexBuilder, camera, partialTick);
        
        x -= playerAnimPos.x;
        y -= playerAnimPos.y;
        z -= playerAnimPos.z;
        xo -= playerAnimPos.x;
        yo -= playerAnimPos.y;
        zo -= playerAnimPos.z;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (user != null) {
            Vec3 offset = user.position().subtract(userPositionPrev);
            move(offset.x, offset.y, offset.z);
            this.userPositionPrev = user.position();
        }
    }
    
    
    public static HamonAuraParticle createCustomParticle(SpriteSet sprites, ClientLevel world, 
            LivingEntity entity, double x, double y, double z) {
        HamonAuraParticle particle = new HamonAura3PersonParticle(world, entity, x, y, z, 0, 0, 0, sprites);
        return particle;
    }
}
