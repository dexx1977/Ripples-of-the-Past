package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

public abstract class StandEntityActionModifier extends StandAction implements IStandPhasedAction {

    public StandEntityActionModifier(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public void perform(Level world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable FriendlyByteBuf extraInput) {
        if (!world.isClientSide() && power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            stand.getCurrentTask().ifPresent(task -> task.addModifierAction(this, stand));
        }
    }
    
    @Override
    public boolean greenSelection(IStandPower power, ActionConditionResult conditionCheck) {
        return conditionCheck.isPositive();
    }
    
    @Override
    public boolean isLegalInHud(IStandPower power) {
        return false;
    }
    
    protected static boolean hasTaskWithNoModifiers(StandEntity standEntity) {
        return standEntity.getCurrentTask().map(task -> !task.hasModifierAction(null)).orElse(false);
    }
    
    public boolean makesAttackNonLethal(LivingEntity target) {
        return false;
    }
    
    
    
    protected class TriggeredFlag {}
}
