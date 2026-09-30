package com.github.standobyte.jojo.action;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.IPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class ActionConditionResult {
    private final boolean positive;
    private final boolean stopHeldAction;
    private final boolean isQueued;
    private final Component warning;
    
    public static final ActionConditionResult POSITIVE = new ActionConditionResult(true, false, false, null);
    public static final ActionConditionResult NEGATIVE = new ActionConditionResult(false, true, false, null);
    public static final ActionConditionResult NEGATIVE_CONTINUE_HOLD = new ActionConditionResult(false, false, false, null);
    public static final ActionConditionResult NEGATIVE_QUEUEABLE = new ActionConditionResult(false, true, true, null);
    
    public static ActionConditionResult createNegative(Component warning) {
        return new ActionConditionResult(false, true, false, warning);
    }
    
    public static ActionConditionResult noMessage(boolean isPositive) {
        return isPositive ? POSITIVE : NEGATIVE;
    }
    
    private ActionConditionResult(boolean positive, boolean stopHeldAction, boolean isQueued, Component warning) {
        this.positive = positive;
        this.stopHeldAction = stopHeldAction;
        this.isQueued = isQueued;
        this.warning = warning;
    }
    
    public ActionConditionResult setContinueHold() {
        return setContinueHold(true);
    }
    
    public ActionConditionResult setContinueHold(boolean continueHold) {
        return new ActionConditionResult(this.positive, !continueHold, this.isQueued, this.warning);
    }
    
    public boolean isPositive() {
        return positive;
    }
    
    public boolean shouldStopHeldAction() {
        return !isPositive() && stopHeldAction;
    }
    
    public boolean isQueued() {
        return isQueued;
    }
    
    @Nullable
    public Component getWarning() {
        return warning;
    }
    
    public static <P extends IPower<P, ?>> void sendActionFailedMessage(Action<P> action, ActionConditionResult result, LivingEntity user) {
        if (!user.level.isClientSide() && action.sendsConditionMessage()) {
            Component message = result.getWarning();
            
            if (message != null && user instanceof ServerPlayer) {
                ((ServerPlayer) user).displayClientMessage(message, true);
            }
        }
    }
}
