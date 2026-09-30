package com.github.standobyte.jojo.power.impl.nonstand.type.zombie;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrZombieDataPacket;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

public class ZombieData extends TypeSpecificData { // TODO remember the vampire that turned the player into zombie (UUID)
    private int lastBloodLevel = -999;
    private boolean disguised = false;

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        LivingEntity user = power.getUser();
        
        if (!user.level.isClientSide()) {
            power.addEnergy(1000);
        }
        
        super.onPowerGiven(oldType, oldData);
    }
    
    public void tick() {
        LivingEntity user = power.getUser();
        if (!user.isAlive()) {
            disguised = false;
        }
    }
    
    public void toggleDisguise() {
        setDisguiseEnabled(!disguised);
    }
    
    public void setDisguiseEnabled(boolean isEnabled) {
        if (this.disguised != isEnabled) {
            this.disguised = isEnabled;
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrZombieDataPacket(user.getId(), this), user);
                power.getType().updatePassiveEffects(user, power);
            }
        }
    }
    
    public boolean isDisguiseEnabled() {
        return disguised;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("DisguiseEnabled", disguised);
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundTag nbt) {
        disguised = nbt.getBoolean("DisguiseEnabled");
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayer user) {
        lastBloodLevel = -999;
    }
    
    public boolean refreshBloodLevel(int bloodLevel) {
        boolean bloodLevelChanged = this.lastBloodLevel != bloodLevel;
        this.lastBloodLevel = bloodLevel;
        return bloodLevelChanged;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayer entity) {
        PacketManager.sendToClient(new TrZombieDataPacket(user.getId(), this), entity);
    }
}
