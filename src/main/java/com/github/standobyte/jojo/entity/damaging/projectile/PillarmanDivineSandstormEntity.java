package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class PillarmanDivineSandstormEntity extends ModdedProjectileEntity {
    private float radius;
    private float damage;
    private float particlesCount;
    private int duration;
    private float xOriginOffset;
    private boolean atmosphericRift;

    public PillarmanDivineSandstormEntity(Level world, LivingEntity entity, float offsetX) {
        super(ModEntityTypes.PILLARMAN_DIVINE_SANDSTORM.get(), entity, world);
        this.xOriginOffset = offsetX;
    }
    
    public PillarmanDivineSandstormEntity setRadius(float radius) {
        this.radius = radius;
        this.particlesCount = isAtmospheric() ? radius * 10 : radius * 2;
        Vec3 pos = getBoundingBox().getCenter();
        refreshDimensions();
        setBoundingBox(new AABB(pos, pos).inflate(radius));
        return this;
    }
    
    public PillarmanDivineSandstormEntity setDamage(float damage) {
        this.damage = damage;
        return this;
    }
    
    public PillarmanDivineSandstormEntity setAtmospheric(boolean atmosphericRift) {
        this.atmosphericRift = atmosphericRift;
        return this;
    }
    
    public boolean isAtmospheric() {
    	return atmosphericRift;
    }
    
    public ParticleOptions setParticle() {
    	if (isAtmospheric()) {
    		return ModParticles.RIFT.get();
    	} else {
    		return ModParticles.SANDSTORM.get();
    	}
    }
    
    public PillarmanDivineSandstormEntity setDuration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public PillarmanDivineSandstormEntity(EntityType<? extends PillarmanDivineSandstormEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        setPos(getX(), getY() - radius, getZ());
        super.shoot(x, y, z, velocity, inaccuracy);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            Vec3 center = getBoundingBox().getCenter();
            int particlesCount = Math.max((int) (this.particlesCount * damageWearOffMultiplier()), 1);
            for (int i = 0; i < particlesCount; i++) {
                Vec3 sparkVec = center.add(new Vec3(
                        (random.nextDouble() - 1.0), 
                        (random.nextDouble() - 1.0),
                        (random.nextDouble() - 1.0))
                        .normalize().scale(random.nextDouble() * radius));
                level.addParticle(setParticle(), false, sparkVec.x, sparkVec.y, sparkVec.z, 0, 0, 0);
            }
        }
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (target instanceof LivingEntity) {
                DamageUtil.knockback3d((LivingEntity) target, radius * 0.035F, xRot, yRot);
            }
        }
    }
    
    @Override
    protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {
        super.afterBlockHit(blockRayTraceResult, blockDestroyed);
        if (level.isClientSide()) {
            Vec3 center = getBoundingBox().getCenter();
            if (isAtmospheric()) {
            	level.playSound(ClientUtil.getClientPlayer(), center.x, center.y, center.z, SoundEvents.WITHER_BREAK_BLOCK, 
                        SoundSource.AMBIENT, 0.3F, 1.0F);
            } else {
            	level.playSound(ClientUtil.getClientPlayer(), center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, 
                        SoundSource.AMBIENT, 0.7F, 1.0F);
            }
        }
        
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (targetType != TargetType.ENTITY) {
            if (!level.isClientSide()) {
                remove();
            } else {
                super.breakProjectile(targetType, hitTarget);
            }
        }
    }
    
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions defaultSize = super.getDimensions(pose);
        return new EntityDimensions(radius * 1.2F, radius * 1.2F, defaultSize.fixed);
    }
    
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public int ticksLifespan() {
        return duration;
    }

    @Override
    protected float getBaseDamage() {
        return damage;
    }
    
    @Override
    protected float knockbackMultiplier() {
        return  0.1F;
    }
    
    @Override
    protected float getDamageAmount() {
        return damage * Math.max(damageWearOffMultiplier(), 0.5F);
    }
    
    private float damageWearOffMultiplier() {
        float ageRatio = (float) tickCount / (float) duration;
        return Math.min(2 - ageRatio * 2, 1);
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 5.0F;
    }
    
    @Override
    public boolean canBeDeflected(@Nullable Entity context) {
        return false;
    }
    
    @Override
    public boolean canBeEvaded(@Nullable Entity context) {
        return false;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return new Vec3((float) xOriginOffset, 0.8F, 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Radius", radius);
        nbt.putFloat("Damage", damage);
        nbt.putInt("Duration", duration);
        nbt.putFloat("Particles", particlesCount);
        nbt.putFloat("XOriginOffset", xOriginOffset);
        nbt.putBoolean("atmosphericRift", atmosphericRift);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setRadius(nbt.getFloat("Radius"));
        damage = nbt.getFloat("Damage");
        duration = nbt.getInt("Duration");
        particlesCount = nbt.getFloat("Particles");
        xOriginOffset = nbt.getFloat("XOriginOffset");
        atmosphericRift = nbt.getBoolean("atmosphericRift");
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(radius);
        buffer.writeVarInt(duration);
        buffer.writeFloat(particlesCount);
        buffer.writeFloat(xOriginOffset);
        buffer.writeBoolean(atmosphericRift);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        setRadius(additionalData.readFloat());
        setDuration(additionalData.readVarInt());
        particlesCount = additionalData.readFloat();
        xOriginOffset = additionalData.readFloat();
        atmosphericRift = additionalData.readBoolean();
    }   
    
}
