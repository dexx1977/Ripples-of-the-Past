package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.item.SatiporojaScarfItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class SatiporojaScarfEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private HumanoidArm side;
    private boolean gaveHamonPoints;
    
    public SatiporojaScarfEntity(Level world, LivingEntity entity, HumanoidArm side) {
        super(ModEntityTypes.SATIPOROJA_SCARF.get(), entity, world);
        this.side = side;
        initYRotOffset();
    }

    public SatiporojaScarfEntity(EntityType<? extends SatiporojaScarfEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    private void initYRotOffset() {
        yRotOffset = side == HumanoidArm.RIGHT ? -67.5F: 67.5F;
    }
    
    @Override
    protected float updateDistance() {
        if (side == HumanoidArm.RIGHT) {
            if (yRotOffset < 67.5F) {
                yRotOffset = Math.min(yRotOffset + 135F / ticksLifespan(), 67.5F);
            }
        }
        else {
            if (yRotOffset > -67.5F) {
                yRotOffset = Math.max(yRotOffset - 135F / ticksLifespan(), -67.5F);
            }
        }
        return super.updateDistance();
    }
    
    @Override
    protected float movementSpeed() {
        return 1.5F;
    }

    @Override
    public int ticksLifespan() {
        return 10;
    }

    @Override
    protected Vec3 originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot, distance);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, 0.6F, this, owner);
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt && !gaveHamonPoints) {
            INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    gaveHamonPoints = true;
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, SatiporojaScarfItem.SCARF_SWING_ENERGY_COST);
                });
            });
        }
    }

    @Override
    public float getBaseDamage() {
        return 0;
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
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("LeftArm", side == HumanoidArm.LEFT);
        nbt.putBoolean("PointsGiven", gaveHamonPoints);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        side = nbt.getBoolean("LeftArm") ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        gaveHamonPoints = nbt.getBoolean("PointsGiven");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(side == HumanoidArm.LEFT);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        side = additionalData.readBoolean() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        initYRotOffset();
    }
}
