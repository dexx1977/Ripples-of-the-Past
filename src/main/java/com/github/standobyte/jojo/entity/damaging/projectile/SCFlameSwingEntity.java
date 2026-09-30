package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class SCFlameSwingEntity extends MRFlameEntity {
    
    public SCFlameSwingEntity(LivingEntity shooter, Level world) {
        super(ModEntityTypes.SC_FLAME.get(), shooter, world);
    }

    public SCFlameSwingEntity(EntityType<? extends SCFlameSwingEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    public int ticksLifespan() {
        return 20;
    }

    private static final Vec3 OFFSET = new Vec3(0.0, -0.3, 0.75);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET;
    }
    
    @Override
    protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, this)) {
                super.afterBlockHit(blockRayTraceResult, blockDestroyed);
            }
        }
    }
}
