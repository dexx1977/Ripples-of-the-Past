package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;

public class IndirectStandEntityDamageSource extends StandEntityDamageSource {
    private final Entity owner;
    private Component standName = null;
    
    public IndirectStandEntityDamageSource(String msgId, DamagingEntity damagingEntity, LivingEntity owner) {
        super(msgId, damagingEntity, IStandPower.getStandPowerOptional(StandUtil.getStandUser(owner)).orElse(null));
        this.standName = null;
        this.owner = owner;
    }
    
    public IndirectStandEntityDamageSource(String msgId, Entity damagingEntity, Entity owner, IStandPower stand) {
        super(msgId, damagingEntity, stand);
        this.owner = owner;
    }
    
    IndirectStandEntityDamageSource(DamageSource damageSource, IStandPower stand) {
        super(damageSource, stand);
        this.owner = damageSource.getEntity();
    }
    
    public IndirectStandEntityDamageSource setStandName(Component standName) {
        this.standName = standName;
        return this;
    }
    
    @Override
    public Entity getDirectEntity() {
        return super.getDirectEntity();
    }
    
    @Override
    public Entity getEntity() {
        return owner;
    }
    
    @Override
    public Component getLocalizedDeathMessage(LivingEntity dead) {
        Component cause = owner != null ? owner.getDisplayName() : standName != null ? standName : getDirectEntity().getDisplayName();
        if (showStandUserName && stand != null) {
            LivingEntity standUser = stand.getUser();
            if (standUser != null) {
                return Component.translatable("death.attack." + getMsgId() + ".stand_user", dead.getDisplayName(), standUser.getDisplayName(), cause);
            }
        }
        return Component.translatable("death.attack." + getMsgId(), dead.getDisplayName(), cause);
    }
    
}
