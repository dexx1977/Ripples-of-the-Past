package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class PillarmanRibEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    protected float knockback = 0;
    private double yOriginOffset;
    private double xOriginOffset;
    private boolean dealtDamage;

    public PillarmanRibEntity(Level world, LivingEntity entity, float angleXZ, float angleYZ, double offsetX, double offsetY) {
        super(ModEntityTypes.PILLARMAN_RIBS.get(), entity, world);
        this.xRotOffset = angleXZ;
        this.yRotOffset = angleYZ;
        this.xOriginOffset = offsetX;
        this.yOriginOffset = offsetY;
    }
    
    public PillarmanRibEntity(EntityType<? extends PillarmanRibEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        super.tick();
    }
    
    @Override
    public float getBaseDamage() {
        return 0.5F;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return !dealtDamage ? super.hurtTarget(target, owner) : false;
    }
    
    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
        	dealtDamage = true;
            Entity target = entityRayTraceResult.getEntity();
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (!JojoModUtil.isTargetBlocking(livingTarget)) {
                    attachToEntity(livingTarget);
                    livingTarget.addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), ticksLifespan() - tickCount));
                }
            }
        }
    }
    
    @Override
    public boolean standDamage() {
        return false;
    }
    
    public void addKnockback(float knockback) {
        this.knockback = knockback;
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }

    @Override
    protected float knockbackMultiplier() {
        return 0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        int ticks = super.ticksLifespan();
        if (isAttachedToAnEntity()) {
            ticks += 20;
        }
        return ticks;
    }
    
    @Override
    protected float movementSpeed() {
        return 8 / (float) ticksLifespan();
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }
    
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return new Vec3(xOriginOffset, yOriginOffset, 0);
    }

    @Override
    protected Vec3 originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot + xRotOffset, distance);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("YRotOffset", yRotOffset);
        nbt.putFloat("XRotOffset", xRotOffset);
        nbt.putFloat("Knockback", knockback);
        nbt.putDouble("YOriginOffset", yOriginOffset);
        nbt.putDouble("XOriginOffset", xOriginOffset);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        yRotOffset = nbt.getFloat("YRotOffset");
        xRotOffset = nbt.getFloat("XRotOffset");
        knockback = nbt.getFloat("Knockback");
        yOriginOffset = nbt.getDouble("YOriginOffset");
        xOriginOffset = nbt.getDouble("XOriginOffset");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(yRotOffset);
        buffer.writeFloat(xRotOffset);
        buffer.writeDouble(xOriginOffset);
        buffer.writeDouble(yOriginOffset);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.xOriginOffset = additionalData.readDouble();
        this.yOriginOffset = additionalData.readDouble();
    }
}
