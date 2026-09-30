package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HamonTurquoiseBlueOverdriveEntity extends ModdedProjectileEntity {
    private float radius;
    private float damage;
    private float points;
    private float sparksCount;
    private boolean gaveHamonPoints;
    private int duration;

    public HamonTurquoiseBlueOverdriveEntity(Level world, LivingEntity entity) {
        super(ModEntityTypes.TURQUOISE_BLUE_OVERDRIVE.get(), entity, world);
    }
    
    public HamonTurquoiseBlueOverdriveEntity setRadius(float radius) {
        this.radius = radius;
        this.sparksCount = radius * radius * 3;
        Vec3 pos = getBoundingBox().getCenter();
        refreshDimensions();
        setBoundingBox(new AABB(pos, pos).inflate(radius));
        return this;
    }
    
    public HamonTurquoiseBlueOverdriveEntity setDamage(float damage) {
        this.damage = damage;
        return this;
    }
    
    public HamonTurquoiseBlueOverdriveEntity setPoints(float points) {
        this.points = points;
        return this;
    }
    
    public HamonTurquoiseBlueOverdriveEntity setDuration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public HamonTurquoiseBlueOverdriveEntity(EntityType<? extends HamonTurquoiseBlueOverdriveEntity> entityType, Level world) {
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
            int sparksCount = Math.max((int) (this.sparksCount * damageWearOffMultiplier()), 1);
            for (int i = 0; i < sparksCount; i++) {
                Vec3 sparkVec = center.add(new Vec3(
                        (random.nextDouble() - 0.5), 
                        (random.nextDouble() - 0.5),
                        (random.nextDouble() - 0.5))
                        .normalize().scale(random.nextDouble() * radius));
                if (level.isWaterAt(new BlockPos(sparkVec))) {
                    level.addParticle(ModParticles.HAMON_SPARK_BLUE.get(), false, sparkVec.x, sparkVec.y, sparkVec.z, 0, 0, 0);
                }
            }
            level.playSound(ClientUtil.getClientPlayer(), center.x, center.y, center.z, ModSounds.HAMON_SPARK.get(), 
                    SoundSource.AMBIENT, Math.min(0.1F + radius * 0.15F, 1), 1.0F + (random.nextFloat() - 0.5F) * 0.15F);
        }
    }

    @Override
    protected void checkHit() {
        if (!level.isClientSide()) {
            if (!this.isInWaterOrBubble()) {
                discard();
                return;
            }
            level.getEntitiesOfClass(LivingEntity.class, getBoundingBox(), entity -> entity.isInWaterOrBubble() && canHitEntity(entity)).forEach(target -> {
                onHitEntity(new EntityHitResult(target));
            });
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, getDamageAmount(), 
                this, owner, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_BLUE.get()));
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (target.isInWaterOrBubble() && target instanceof LivingEntity) {
                DamageUtil.knockback3d((LivingEntity) target, radius * 0.1F, xRot, yRot);
            }
            if (!gaveHamonPoints) {
                INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        gaveHamonPoints = true;
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, points);
                    });
                });
            }
        }
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (targetType != TargetType.ENTITY) {
            super.breakProjectile(targetType, hitTarget);
        }
    }
    
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions defaultSize = super.getDimensions(pose);
        return new EntityDimensions(radius * 2, radius * 2, defaultSize.fixed);
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
    protected float getDamageAmount() {
        return damage * damageWearOffMultiplier();
    }
    
    private float damageWearOffMultiplier() {
        float ageRatio = (float) tickCount / (float) duration;
        return Math.min(2 - ageRatio * 2, 1);
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
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
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Radius", radius);
        nbt.putBoolean("PointsGiven", gaveHamonPoints);
        nbt.putFloat("Damage", damage);
        nbt.putFloat("Points", points);
        nbt.putInt("Duration", duration);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setRadius(nbt.getFloat("Radius"));
        gaveHamonPoints = nbt.getBoolean("PointsGiven");
        damage = nbt.getFloat("Damage");
        points = nbt.getFloat("Points");
        duration = nbt.getInt("Duration");
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(radius);
        buffer.writeVarInt(duration);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        setRadius(additionalData.readFloat());
        setDuration(additionalData.readVarInt());
    }
}
