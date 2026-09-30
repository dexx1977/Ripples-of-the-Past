package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.ResolveCounter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HGEmeraldEntity extends ModdedProjectileEntity {
    @Nullable
    private IStandPower userStandPower;
    private boolean lowerKnockback;
    private boolean breakBlocks;

    public HGEmeraldEntity(LivingEntity shooter, Level world, @Nullable IStandPower standPower) {
        super(ModEntityTypes.HG_EMERALD.get(), shooter, world);
        userStandPower = standPower;
    }

    public HGEmeraldEntity(EntityType<? extends HGEmeraldEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }

    @Override
    public float getBaseDamage() {
        return 1F;
    }
    
    @Override
    protected float knockbackMultiplier() {
        return lowerKnockback ? 0.5F : super.knockbackMultiplier();
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return breakBlocks ? 1.5F : 0.0F;
    }

    @Override
    public int ticksLifespan() {
        return 100;
    }
    
    public void setLowerKnockback(boolean lowerKnockback) {
        this.lowerKnockback = lowerKnockback;
    }
    
    public void setBreakBlocks(boolean breakBlocks) {
        this.breakBlocks = breakBlocks;
    }

    @Override
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {
        if (!level.isClientSide() && entityHurt && userStandPower != null) {
            Entity target = entityRayTraceResult.getEntity();
            if (ResolveCounter.attackingTargetGivesResolve(target)) {
                userStandPower.addLearningProgressPoints(ModStandsInit.HIEROPHANT_GREEN_EMERALD_SPLASH.get(), 0.002F);
            }
        }
    }

    private static final Vec3 OFFSET = new Vec3(0.0, -0.3, 0.75);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET;
    }
}
