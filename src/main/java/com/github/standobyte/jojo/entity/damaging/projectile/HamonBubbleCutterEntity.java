package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HamonBubbleCutterEntity extends ModdedProjectileEntity { // TODO bubble lenses
    private boolean gliding;
    private float hamonStatPoints;
    
    public HamonBubbleCutterEntity(LivingEntity shooter, Level world) {
        super(ModEntityTypes.HAMON_BUBBLE_CUTTER.get(), shooter, world);
    }

    public HamonBubbleCutterEntity(EntityType<? extends HamonBubbleCutterEntity> type, Level world) {
        super(type, world);
    }
    
    public void setGliding(boolean gliding) {
        this.gliding = gliding;
    }
    
    public void setHamonStatPoints(float points) {
        this.hamonStatPoints = points;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            HamonSparksLoopSound.playSparkSound(this, position(), 0.25F);
            CustomParticlesHelper.createHamonSparkParticles(this, position(), 1);
        }
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        boolean projectileAttack = super.hurtTarget(target, owner);
        boolean hamonAttack = DamageUtil.dealHamonDamage(target, 0.3F, this, owner);
        return projectileAttack || hamonAttack;
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, hamonStatPoints);
                    });
                });
            }
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (gliding && result.getDirection().getAxis() == Axis.Y) {
            Vec3 movementVec = getDeltaMovement();
            Vec3 newVec = new Vec3(movementVec.x, 0, movementVec.z);
            this.setDeltaMovement(newVec.scale(Math.sqrt(movementVec.lengthSqr() / newVec.lengthSqr())));
        }
        else {
            super.onHitBlock(result);
        }
    }
    
    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 1.0F;
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
        nbt.putBoolean("Gliding", gliding);
        nbt.putFloat("Points", hamonStatPoints);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        gliding = nbt.getBoolean("Gliding");
        hamonStatPoints = nbt.getFloat("Points");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(gliding);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.gliding = additionalData.readBoolean();
    }
}
