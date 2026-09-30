package com.github.standobyte.jojo.entity.ai;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.server.level.ServerLevel;

public class SpecificTargetGoal extends TargetGoal {
    private final UUID targetUuid;
    private LivingEntity targetEntity;

    public SpecificTargetGoal(Mob mob, UUID target, boolean mustSee, boolean mustReach) {
        super(mob, mustSee, mustReach);
        this.targetUuid = target;
    }
    
    @Nullable
    private LivingEntity getTargetEntity() {
        if (targetUuid == null) return null;
        
        if (targetEntity != null) {
            if (targetEntity.isAlive()) {
                return targetEntity;
            }
            else {
                targetEntity = null;
            }
        }
        
        if (!mob.level.isClientSide()) {
            Entity entity = ((ServerLevel) mob.level).getEntity(targetUuid);
            if (entity instanceof LivingEntity) {
                targetEntity = (LivingEntity) entity;
            }
        }
        
        return targetEntity;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = getTargetEntity();
        return target != null && target.isAlive();
    }
    
    @Override
    public void start() {
        LivingEntity target = getTargetEntity();
        if (target != null) {
            mob.setTarget(target);
            this.targetMob = mob.getTarget();
        }
        super.start();
    }

}
