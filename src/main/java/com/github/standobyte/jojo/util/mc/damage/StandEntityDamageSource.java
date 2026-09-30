package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

public class StandEntityDamageSource extends DamageSource implements IStandDamageSource, IModdedDamageSource {
    protected final IStandPower stand;
//    @Nullable
//    protected final Entity standUser;
    private float knockbackFactor = 1;
    private boolean stackKnockback = false;
    private boolean bypassInvulTicks = false;
    private boolean preventDamagingArmor = false;
    private boolean nonLethal = false;
    protected boolean showStandUserName;
    private int barrageHits = 0;
    private int standInvulTicks = 0;
    private boolean standCanHitSelf = false;

    public StandEntityDamageSource(String msgId, Entity damagingEntity, IStandPower stand) {
        super(ModDamageTypes.holder(damagingEntity, msgId), damagingEntity);
        this.stand = stand;
    }
    
    StandEntityDamageSource(DamageSource damageSource, IStandPower stand) {
        super(damageSource.typeHolder(), damageSource.getDirectEntity());
        this.stand = stand;
    }
    
    @Override
    public IStandPower getStandPower() {
        return stand;
    }

    @Override
    public boolean scalesWithDifficulty() {
        if (super.scalesWithDifficulty()) {
            if (stand == null) {
               return true;
            } else {
               LivingEntity standUser = stand.getUser();
               return standUser != null && standUser instanceof LivingEntity 
                       && !(standUser instanceof Player);
            }
        }
        return false;
    }
    
    public StandEntityDamageSource setBarrageHitsCount(int hits) {
        this.barrageHits = hits;
        return this;
    }
    
    public int getBarrageHitsCount() {
        return barrageHits;
    }

    @Override
    public StandEntityDamageSource setKnockbackReduction(float factor) {
        this.knockbackFactor = Mth.clamp(factor, 0, 1);
        return this;
    }

    @Override
    public float getKnockbackFactor() {
        return knockbackFactor;
    }
    
    
    @Override
    public StandEntityDamageSource setStackKnockback() {
        this.stackKnockback = true;
        return this;
    }
    
    @Override
    public boolean doesStackKnockback() {
        return stackKnockback;
    }
    

    @Override
    public StandEntityDamageSource setBypassInvulTicksInEvent() {
        this.bypassInvulTicks = true;
        return this;
    }

    @Override
    public boolean bypassInvulTicks() {
        return bypassInvulTicks;
    }
    
    
    @Override
    public StandEntityDamageSource setPreventDamagingArmor() {
        this.preventDamagingArmor = true;
        return this;
    }
    
    @Override
    public boolean preventsDamagingArmor() {
        return preventDamagingArmor;
    }
    
    
    @Override
    public StandEntityDamageSource setNonLethal() {
        this.nonLethal = true;
        return this;
    }
    
    @Override
    public boolean isNonLethal() {
        return nonLethal;
    }
    
    
    @Override
    public boolean canHurtStands() {
        return true;
    }
    

    public StandEntityDamageSource setStandInvulTicks(int ticks) {
        this.standInvulTicks = ticks;
        return this;
    }

    @Override
    public int getStandInvulTicks() {
        return standInvulTicks;
    }
    
    @Override
    public StandEntityDamageSource setStandCanHitSelf() {
        standCanHitSelf = true;
        return this;
    }

    @Override
    public boolean standCanHitSelf() {
        return standCanHitSelf;
    }

    public StandEntityDamageSource setShowStandUserName() {
        this.showStandUserName = true;
        return this;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity dead) {
        if (showStandUserName && stand != null) {
            LivingEntity standUser = stand.getUser();
            if (standUser != null) {
                return Component.translatable("death.attack." + getMsgId() + ".stand_user", dead.getDisplayName(), standUser.getDisplayName(), getDirectEntity().getDisplayName());
            }
        }
        return Component.translatable("death.attack." + getMsgId(), dead.getDisplayName(), getDirectEntity().getDisplayName());
    }

    @Override
    public String toString() {
       return "StandEntityDamageSource (" + this.getDirectEntity() + ")";
    }
}
