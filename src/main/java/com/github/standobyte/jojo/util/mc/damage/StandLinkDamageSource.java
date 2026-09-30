package com.github.standobyte.jojo.util.mc.damage;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

public class StandLinkDamageSource extends DamageSource {
    private final Entity standEntity;
    private final DamageSource actualSource;

    public StandLinkDamageSource(Entity standEntity, DamageSource actualSource) {
        // the armour/magic bypass lives in the data driven damage type now
        super(ModDamageTypes.holder(standEntity, "healthLink"), standEntity);
        this.standEntity = standEntity;
        this.actualSource = actualSource;
    }

//    @Override
//    public boolean isBypassArmor() {
//        return true;
//    }
//
//    @Override
//    public boolean isBypassMagic() {
//        return true;
//    }
//
//
//    
//    @Override
//    public boolean isProjectile() {
//        return actualSource.isProjectile();
//    }
//
//    @Override
//    public boolean isExplosion() {
//        return actualSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
//    }
//
//    @Override
//    public float getFoodExhaustion() {
//        return actualSource.getFoodExhaustion();
//    }
//
//    @Override
//    public boolean isBypassInvul() {
//        return actualSource.isBypassInvul();
//    }
//
//    @Override
//    public boolean isFire() {
//        return actualSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
//    }
//
//    @Override
//    public boolean scalesWithDifficulty() {
//        return actualSource.scalesWithDifficulty();
//    }
//
//    @Override
//    public boolean isMagic() {
//        return actualSource.isMagic();
//    }
//
//    @Override
//    public boolean isCreativePlayer() {
//        return actualSource.isCreativePlayer();
//    }



    @Override
    public String toString() {
       return "DamageSource (" + getMsgId() + " (" + actualSource.getMsgId() + "))";
    }

    @Nullable
    public Entity getDirectEntity() {
        return actualSource.getDirectEntity();
    }

    @Nullable
    public Entity getEntity() {
        return actualSource.getEntity();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity dead) {
        return actualSource.getLocalizedDeathMessage(dead);
    }

    @Override
    public String getMsgId() {
        return actualSource.getMsgId();
    }
    
    public DamageSource getOriginalDamageSource() {
        return actualSource;
    }

    @Override
    @Nullable
    public Vec3 getSourcePosition() {
        return null;
    }
    
    public Entity getStandEntity() {
        return standEntity;
    }
}
