package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PillarmanHideInEntity extends PillarmanAction {

    public PillarmanHideInEntity(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity targetEntity = target.getEntity();
        return ActionConditionResult.noMessage(targetEntity != null && targetEntity instanceof LivingEntity && !(targetEntity.is(user)));
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide() && user instanceof IPlayerPossess) {
            Entity targetEntity = target.getEntity();
            ((IPlayerPossess) user).jojoPossessEntity(targetEntity, true, this);
        }
    }
    
    @Override
    public boolean clHeldStartAnim(Player user) {
        return ModPlayerAnimations.pillarmanPossession.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.pillarmanPossession.setAnimEnabled(user, false);
    }
}
