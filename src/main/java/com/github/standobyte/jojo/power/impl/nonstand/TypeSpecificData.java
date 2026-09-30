package com.github.standobyte.jojo.power.impl.nonstand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

public abstract class TypeSpecificData {
    protected INonStandPower power;
    protected Optional<ServerPlayer> serverPlayer;
    
    public void setPower(INonStandPower power) {
        this.power = power;
        LivingEntity user = power.getUser();
        this.serverPlayer = user instanceof ServerPlayer ? Optional.of((ServerPlayer) user) : Optional.empty();
    }
    
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower powerData) {
        return true;
    }
    
    public void onPowerGiven(@Nullable NonStandPowerType<?> oldType, @Nullable TypeSpecificData oldData) {
        // in creative, this sets time stop to the maximum duration considering the power type (e.g. 9s for vampires)
        LivingEntity user = power.getUser();
        if (user != null && !user.level.isClientSide() && StandPower.playerSkipsActionTraining(user)) {
            IStandPower.getStandPowerOptional(user).ifPresent(stand -> {
                if (stand.hasPower() && stand.wasProgressionSkipped()) {
                    stand.skipProgression();
                }
            });
        }
    }
    
    public abstract CompoundTag writeNBT();
    public abstract void readNBT(CompoundTag nbt);
    
    public abstract void syncWithUserOnly(ServerPlayer user);
    public abstract void syncWithTrackingOrUser(LivingEntity user, ServerPlayer entity);
}
