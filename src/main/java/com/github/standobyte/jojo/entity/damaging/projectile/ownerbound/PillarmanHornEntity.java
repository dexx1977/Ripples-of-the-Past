package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class PillarmanHornEntity extends OwnerBoundProjectileEntity {

    public PillarmanHornEntity(Level world, LivingEntity entity) {
        super(ModEntityTypes.PILLARMAN_HORN.get(), entity, world);
    }
    
    public PillarmanHornEntity(EntityType<? extends PillarmanHornEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 0.5F;
    }

    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }

    @Override
    protected float knockbackMultiplier() {
        return 0;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 5.0F;
    }
    
    @Override
    protected float movementSpeed() {
        return 0.4F;
    }
    
    @Override
    protected int timeAtFullLength() {
        return 4;
    }
    
    @Override
    protected float retractSpeed() {
        return movementSpeed() * 3F;
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }

    private static final Vec3 OFFSET = new Vec3(0, 0.15F, 0);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET;
    }
}
