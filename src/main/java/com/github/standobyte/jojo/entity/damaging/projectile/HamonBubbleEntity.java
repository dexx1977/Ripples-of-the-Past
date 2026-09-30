package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

public class HamonBubbleEntity extends ModdedProjectileEntity {
    
    public HamonBubbleEntity(LivingEntity shooter, Level world) {
        super(ModEntityTypes.HAMON_BUBBLE.get(), shooter, world);
    }

    public HamonBubbleEntity(EntityType<? extends HamonBubbleEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide() && tickCount % 10 == getId() % 10) {
            HamonSparksLoopSound.playSparkSound(this, position(), 0.25F, true);
            CustomParticlesHelper.createHamonSparkParticles(this, position(), 1);
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, 0.3F, this, owner);
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, ModHamonActions.CAESAR_BUBBLE_LAUNCHER.get().getHeldTickEnergyCost(power) / 4F);
                    });
                });
            }
        }
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 0;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        return 100;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
    }
}
