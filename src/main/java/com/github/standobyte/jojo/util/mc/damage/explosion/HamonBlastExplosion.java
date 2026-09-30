package com.github.standobyte.jojo.util.mc.damage.explosion;

import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

public class HamonBlastExplosion extends CustomExplosion {
    private float hamonDamage;

    public HamonBlastExplosion(Level pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
        super(pLevel, pToBlowX, pToBlowY, pToBlowZ, pRadius);
    }
    
    public HamonBlastExplosion(Level pLevel, Entity pSource, 
            ExplosionDamageCalculator pDamageCalculator, 
            double pToBlowX, double pToBlowY, double pToBlowZ, 
            float pRadius) {
        super(pLevel, pSource, 
                null, pDamageCalculator, 
                pToBlowX, pToBlowY, pToBlowZ, 
                pRadius, false, Explosion.BlockInteraction.KEEP);
    }
    
    public void setHamonDamage(float hamonDamage) {
        this.hamonDamage = hamonDamage;
    }
    
    @Override
    protected List<Entity> getAffectedEntities(AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, 
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS).and(entity -> !entity.is(getExploder())))
                .stream().collect(Collectors.toList());
    }
    
    @Override
    protected float calcDamage(double impact, double diameter) {
        return super.calcDamage(impact, diameter) * hamonDamage;
    }
    
    @Override
    protected void hurtEntity(Entity entity, float damage, double knockback, Vec3 vecToEntityNorm) {
        DamageUtil.dealHamonDamage(entity, damage, getIndirectSourceEntity(), null, HamonAttackProperties::noSrcEntityHamonMultiplier);
    }
    
    @Override
    public void finalizeExplosion(boolean pSpawnParticles) {
        if (level.isClientSide) {
            playSound();
        }
        
        if (pSpawnParticles) {
            spawnParticles();
        }
        
        chargeBlocksWithHamon();
    }
    
    protected void chargeBlocksWithHamon() {
        
    }
    
    // FIXME hamon blast visuals & sound
    @Override
    protected void playSound() {
        Vec3 pos = getPosition();
//        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 
//                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
    }
    
    @Override
    protected void spawnParticles() {
        Vec3 pos = getPosition();
//        if (radius >= 2.0F && blockInteraction != Explosion.Mode.NONE) {
//            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
//        } else {
//            level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
//        }
    }
    
    // FIXME charge nearby living blocks
    
    @Deprecated // not the actual DamageSource object
    @Override
    public DamageSource getDamageSource() {
        return DamageUtil.damageSource(level, DamageUtil.HAMON);
    }
    
    @Override
    public ResourceLocation getExplosionType() {
        return CustomExplosion.Register.HAMON;
    }
}
