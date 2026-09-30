package com.github.standobyte.jojo.entity.ai;

import java.util.function.Predicate;

import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class ZombieNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final HungryZombieEntity zombie;

    public ZombieNearestAttackableTargetGoal(HungryZombieEntity zombie, Class<T> targetClass, boolean mustSee) {
        this(zombie, targetClass, mustSee, false);
    }

    public ZombieNearestAttackableTargetGoal(HungryZombieEntity zombie, Class<T> targetClass, boolean mustSee,
            boolean mustReach) {
        this(zombie, targetClass, 10, mustSee, mustReach, null);
    }

    public ZombieNearestAttackableTargetGoal(HungryZombieEntity zombie, Class<T> targetClass, int randomInterval,
            boolean mustSee, boolean mustReach, Predicate<LivingEntity> targetCondition) {
        super(zombie, targetClass, randomInterval, mustSee, mustReach, targetCondition);
        this.zombie = zombie;
    }
    
    @Override
    public boolean canUse() {
        if (super.canUse()) {
            LivingEntity owner = zombie.getOwner();
            if (owner == null || zombie.farFromOwner(16)) {
                return true;
            }
        }
        return false;
    }

}
