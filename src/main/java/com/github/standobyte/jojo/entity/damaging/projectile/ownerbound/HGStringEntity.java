package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HGStringEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    private boolean isBinding;
    private boolean dealtDamage;
    private float knockback = 0;

    public HGStringEntity(Level world, LivingEntity entity, float angleXZ, float angleYZ, boolean isBinding) {
        super(ModEntityTypes.HG_STRING.get(), entity, world);
        this.yRotOffset = angleXZ;
        this.xRotOffset = angleYZ;
        this.isBinding = isBinding;
    }
    
    public HGStringEntity(EntityType<? extends HGStringEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    public boolean isBinding() {
        return isBinding;
    }
    
    @Override
    public float getBaseDamage() {
        return isBinding ? 0.5F : 1F;
    }
    
    public void addKnockback(float knockback) {
        this.knockback = knockback;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return !dealtDamage ? super.hurtTarget(target, owner) : false;
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }
    
    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            dealtDamage = true;
            Entity target = entityRayTraceResult.getEntity();
            if (isBinding) {
                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    if (!JojoModUtil.isTargetBlocking(livingTarget)) {
                        attachToEntity(livingTarget);
                        livingTarget.addEffect(new MobEffectInstance(ModStatusEffects.IMMOBILIZE.get(), ticksLifespan() - tickCount));
                    }
                }
            }
            else {
                if (knockback > 0 && target instanceof LivingEntity) {
                    DamageUtil.knockback((LivingEntity) target, knockback, yRot);
                }
                setIsRetracting(true);
            }
        }
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
        if (isBinding && isAttachedToAnEntity()) {
            ticks += 10;
        }
        return ticks;
    }
    
    @Override
    protected float movementSpeed() {
        return 16 / (float) ticksLifespan();
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }
    
    private static final Vec3 FRONT_OFFSET = new Vec3(0.0D, 0.0D, 0.25D);
    @Override
    protected Vec3 getXRotOffset() {
        return FRONT_OFFSET;
    }

    @Override
    protected Vec3 originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot + xRotOffset, distance);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(yRotOffset);
        buffer.writeFloat(xRotOffset);
        buffer.writeBoolean(isBinding);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.isBinding = additionalData.readBoolean();
    }
}
