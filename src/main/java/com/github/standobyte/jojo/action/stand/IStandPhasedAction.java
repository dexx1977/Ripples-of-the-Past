package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.level.Level;

public interface IStandPhasedAction {
    default void standTickButtonHold(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    default void standTickWindup(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    default boolean standCanTickPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) { return true; }
    
    default void standTickPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    default boolean standCanPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) { return true; }
    
    default void standPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    default void standTickRecovery(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    default void phaseTransition(Level world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {}
    
    default int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) { return 0; }

    default int getStandActionTicks(IStandPower standPower, StandEntity standEntity) { return 1; }
    
    default int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) { return 0; }
    
    default float getStaminaCost(IStandPower stand) { return 0; }
    
    default float getStaminaCostTicking(IStandPower stand) { return 0; }
}
