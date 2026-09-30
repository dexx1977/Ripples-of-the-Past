package com.github.standobyte.jojo.entity.ai;

import java.util.EnumSet;

import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

public class ZombieOwnerHurtByTargetGoal extends TargetGoal {
    private final HungryZombieEntity zombie;
    private LivingEntity attacker;
    private int timestamp;

    public ZombieOwnerHurtByTargetGoal(HungryZombieEntity zombie) {
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
            attacker = owner.getLastHurtByMob();
            int i = owner.getLastHurtByMobTimestamp();
            return i != timestamp && canAttack(attacker, TargetingConditions.DEFAULT) && zombie.wantsToAttack(attacker, owner);
        }
    }


    @Override
    public void start() {
        mob.setTarget(attacker);
        LivingEntity owner = zombie.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}