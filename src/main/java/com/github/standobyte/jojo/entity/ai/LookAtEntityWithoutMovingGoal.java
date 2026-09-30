package com.github.standobyte.jojo.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.player.Player;

public class LookAtEntityWithoutMovingGoal extends InteractGoal {
    
    public LookAtEntityWithoutMovingGoal(Mob mob, LivingEntity lookAtEntity) {
        super(mob, Player.class, 8, 1);
        this.lookAt = lookAtEntity;
    }
    
    @Override
    public boolean canUse() {
        return lookAt != null;
    }
}
