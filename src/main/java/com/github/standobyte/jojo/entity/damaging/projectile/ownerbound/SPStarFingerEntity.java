package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class SPStarFingerEntity extends OwnerBoundProjectileEntity {

    public SPStarFingerEntity(Level world, LivingEntity entity) {
        super(ModEntityTypes.SP_STAR_FINGER.get(), entity, world);
    }
    
    public SPStarFingerEntity(EntityType<? extends SPStarFingerEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 4.5F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 5.0F;
    }
    
    @Override
    protected float movementSpeed() {
        return 0.3F;
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

    private static final Vec3 OFFSET = new Vec3(-0.3, -0.2, 0.75);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET;
    }
}
