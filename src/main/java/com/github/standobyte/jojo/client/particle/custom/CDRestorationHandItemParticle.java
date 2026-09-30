package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.client.particle.CDRestorationParticle;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public class CDRestorationHandItemParticle extends EntityPosParticle {
    private final LivingEntity livingEntity;
    private final Vec3 randomOffset;
    private final InteractionHand hand;

    private CDRestorationHandItemParticle(ClientLevel world, LivingEntity entity, InteractionHand hand) {
        super(world, entity, true);
        this.livingEntity = entity;
        this.hand = hand;
        this.randomOffset = new Vec3((random.nextDouble() - 0.5), (random.nextDouble() - 0.5), (random.nextDouble() - 0.5)).scale(0.5);
        initPos();
    }
    
    @Override
    protected Vec3 getNextTickPos(Vec3 entityPos) {
        return livingEntity != null ? entityPos.add(randomOffset).add(new Vec3(
                livingEntity.getBbWidth() * 0.6 * (livingEntity.getMainArm() == (hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT) ? -1 : 1), 
                livingEntity.getBbHeight() * (entity.isShiftKeyDown() ? 0.25 : 0.45), 
                livingEntity.getBbWidth() * 0.7)
                .yRot(-livingEntity.yBodyRot * MathUtil.DEG_TO_RAD)) : null;
    }
    
    public static EntityPosParticle createCustomParticle(ClientLevel world, LivingEntity entity, InteractionHand hand) {
        EntityPosParticle particle = new CDRestorationHandItemParticle(world, entity, hand);
        particle.pickSprite(CDRestorationParticle.Factory.getSprite());
        particle.setLifetime(5);
        return particle;
    }

}
