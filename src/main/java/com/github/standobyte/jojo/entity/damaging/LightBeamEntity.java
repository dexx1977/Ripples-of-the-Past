package com.github.standobyte.jojo.entity.damaging;

import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class LightBeamEntity extends DamagingEntity {
    protected HitResult target;
    protected float length;
    protected float damage;

    public LightBeamEntity(EntityType<? extends LightBeamEntity> entityType, LivingEntity shooter, Level world) {
        super(entityType, shooter, world);
    }

    public LightBeamEntity(EntityType<? extends LightBeamEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    public void shoot(float damage, float length) {
        this.damage = damage;
        this.length = length;
        LivingEntity shooter = getOwner();
        if (shooter != null) {
            target = rayTrace()[0];
            if (target.getType() != HitResult.Type.MISS) {
                length = Mth.sqrt(shooter.distanceToSqr(target.getLocation()));
            }
        }
    }

    @Override
    public HitResult[] rayTrace() {
        return new HitResult[] { JojoModUtil.rayTrace(this, length, e -> e != getOwner()) };
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        if (!level.isClientSide()) {
            Entity target = entityRayTraceResult.getEntity();
            target.setSecondsOnFire((int) damage / 2);
            if (DamageUtil.entityTakesUVDamage(target, false)) {
                DamageUtil.dealUltravioletDamage(target, damage, this, getOwner(), false);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockRayTraceResult) {
        if (!level.isClientSide()) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos().relative(blockRayTraceResult.getDirection());
            if (level.isEmptyBlock(blockPos)) {
                level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
            }
        }
    }
    
    @Override
    public float getBaseDamage() {
        return damage;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 2.5F;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }
    
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().expandTowards(getEndPoint().subtract(position()));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return super.shouldRenderAtSqrDistance(distance - length * length);
    }
    
    public Vec3 getEndPoint() {
        return position().add(Vec3.directionFromRotation(xRot, yRot).scale(length));
    }
    
    public float getLength() {
        return length;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("Length", length);
        nbt.putFloat("Damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        damage = nbt.getFloat("Damage");
        length = nbt.getFloat("Length");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(length);
        buffer.writeFloat(damage);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        length = additionalData.readFloat();
        damage = additionalData.readFloat();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public int ticksLifespan() {
        return 1;
    }

}
