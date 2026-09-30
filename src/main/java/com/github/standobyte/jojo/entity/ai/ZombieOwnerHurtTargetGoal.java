package com.github.standobyte.jojo.entity.ai;

import java.util.EnumSet;

import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.damagesource.DamageSource;

public class ZombieOwnerHurtTargetGoal extends TargetGoal {
    private final HungryZombieEntity zombie;
    private LivingEntity attacked;
    private int timestamp;

    public ZombieOwnerHurtTargetGoal(HungryZombieEntity zombie) {
        super(zombie, false);
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = zombie.getOwner();
        if (owner == null || zombie.farFromOwner(12)) {
            return false;
        } else {
            this.attacked = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            DamageSource attackedBy = attacked != null ? attacked.getLastDamageSource() : null;
            return i != timestamp && !(attackedBy != null && !attackedBy.getMsgId().startsWith("bloodDrain")) &&
                    canAttack(attacked, TargetingConditions.DEFAULT) && zombie.wantsToAttack(attacked, owner);
        }
    }

    @Override
    public void start() {
        mob.setTarget(attacked);
        LivingEntity owner = zombie.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}