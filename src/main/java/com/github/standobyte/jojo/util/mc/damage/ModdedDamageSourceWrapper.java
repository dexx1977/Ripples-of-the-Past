package com.github.standobyte.jojo.util.mc.damage;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

public class ModdedDamageSourceWrapper extends DamageSource implements IModdedDamageSource {
    private final DamageSource dmgSource;
    private float knockbackFactor = 1;
    private boolean stackKnockback = false;
    private boolean bypassInvulTicks = false;
    private boolean preventDamagingArmor = false;
    private boolean nonLethal = false;
    protected boolean showStandUserName;
    protected boolean canHurtStands;

    public ModdedDamageSourceWrapper(DamageSource dmgSource) {
        super(dmgSource.typeHolder(), dmgSource.getDirectEntity(), dmgSource.getEntity());
        this.dmgSource = dmgSource;
    }
    
    
    
    @Override
    public ModdedDamageSourceWrapper setKnockbackReduction(float factor) {
        this.knockbackFactor = Mth.clamp(factor, 0, 1);
        return this;
    }
    
    @Override
    public float getKnockbackFactor() {
        return knockbackFactor;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setStackKnockback() {
        this.stackKnockback = true;
        return this;
    }
    
    @Override
    public boolean doesStackKnockback() {
        return stackKnockback;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setBypassInvulTicksInEvent() {
        this.bypassInvulTicks = true;
        return this;
    }
    
    @Override
    public boolean bypassInvulTicks() {
        return bypassInvulTicks;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setPreventDamagingArmor() {
        this.preventDamagingArmor = true;
        return this;
    }
    
    @Override
    public boolean preventsDamagingArmor() {
        return preventDamagingArmor;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setNonLethal() {
        this.nonLethal = true;
        return this;
    }
    
    @Override
    public boolean isNonLethal() {
        return nonLethal;
    }
    
    
    public ModdedDamageSourceWrapper setCanHurtStands() {
        this.canHurtStands = true;
        return this;
    }
    
    @Override
    public boolean canHurtStands() {
        return canHurtStands;
    }
    
    
    
    // redirect all the vanilla methods (except Thorns armor damage from EntityDamageSource) to the wrapped DamageSoruce instance
    public String toString() {
        return "RotP wrapper of " + dmgSource.toString();
    }
    
    public boolean isProjectile() {
        return dmgSource.isProjectile();
    }
    
    public boolean isExplosion() {
        return dmgSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
    }
    
    public boolean isBypassArmor() {
        return dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
    }
    
    public float getFoodExhaustion() {
        return dmgSource.getFoodExhaustion();
    }
    
    public boolean isBypassInvul() {
        return dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
    }
    
    public boolean isBypassMagic() {
        return dmgSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_RESISTANCE);
    }
    
    @Nullable
    public Entity getDirectEntity() {
        return dmgSource.getDirectEntity();
    }
    
    @Nullable
    public Entity getEntity() {
        return dmgSource.getEntity();
    }
    
    public Component getLocalizedDeathMessage(LivingEntity pLivingEntity) {
        return dmgSource.getLocalizedDeathMessage(pLivingEntity);
    }
    
    public boolean isFire() {
        return dmgSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
    }
    
    public String getMsgId() {
        return dmgSource.getMsgId();
    }
    
    public boolean scalesWithDifficulty() {
        return dmgSource.typeHolder().value().scaling() == net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
    }
    
    public boolean isMagic() {
        return dmgSource.is(com.github.standobyte.jojo.init.ModTags.MAGIC);
    }
    
    public boolean isCreativePlayer() {
        return dmgSource.isCreativePlayer();
    }
    
    @Nullable
    public Vec3 getSourcePosition() {
        return dmgSource.getSourcePosition();
    }
}
