package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class SCRapierEntity extends ModdedProjectileEntity {
    private static final int MAX_RICOCHETS = 100;
    private int ricochetCount;
    
    public SCRapierEntity(LivingEntity shooter, Level world) {
        super(ModEntityTypes.SC_RAPIER.get(), shooter, world);
    }

    public SCRapierEntity(EntityType<? extends SCRapierEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    protected float getBaseDamage() {
        LivingEntity owner = getOwner();
        float damage;
        if (owner != null) {
            damage = (float) owner.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        }
        else {
            damage = (float) ModStands.SILVER_CHARIOT.getStandType().getStats().getBasePower();
        }
        return damage * 1.5F;
    }
    
    @Override
    protected float getDamageFinalCalc(float damage) {
        return damage + (float) ricochetCount * 0.5F;
    }
    
    @Override
    protected boolean debuffsFromStand() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof Skeleton && random.nextFloat() < 0.05F);
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    public int ticksLifespan() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
    }
    
    @Override
    protected void onHitBlock(BlockHitResult blockRayTraceResult) {
        boolean ricochet = false;
        if (ricochetCount < MAX_RICOCHETS) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            SoundType soundType = blockState.getSoundType(level, blockPos, this);
            level.playSound(null, blockPos, soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
            Direction hitFace = blockRayTraceResult.getDirection();
            ricochet = ricochet(hitFace);
        }
        if (!ricochet) {
            Vec3 pos = position();
            Vec3 movementVec = getDeltaMovement();
            Direction hitFace = blockRayTraceResult.getDirection();
            Vec3 blockVec = Vec3.atCenterOf(blockRayTraceResult.getBlockPos()).add(Vec3.atLowerCornerOf(hitFace.getNormal()).scale(0.5));
            double k;
            switch (hitFace.getAxis()) {
            case X:
                k = (blockVec.x - pos.x) / movementVec.x;
                break;
            case Y:
                k = (blockVec.y - pos.y) / movementVec.y;
                break;
            case Z:
                k = (blockVec.z - pos.z) / movementVec.z;
                break;
            default:
                return;
            }
            setPos(
                    getX() + movementVec.x * k, 
                    getY() + movementVec.y * k, 
                    getZ() + movementVec.z * k);
            setDeltaMovement(Vec3.ZERO);
        }
    }
    
    private boolean ricochet(Direction hitSurfaceDirection) {
        if (hitSurfaceDirection != null) {
            Vec3 motion = getDeltaMovement();
            Vec3 motionNew;
            switch (hitSurfaceDirection.getAxis()) {
            case X:
                motionNew = new Vec3(-motion.x, motion.y, motion.z);
                break;
            case Y:
                motionNew = new Vec3(motion.x, -motion.y, motion.z);
                break;
            case Z:
                motionNew = new Vec3(motion.x, motion.y, -motion.z);
                break;
            default:
                return false;
            }
            if (JojoModUtil.rayTrace(position(), motionNew, 16, level, this, 
                    EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE), 1.0, 0).getType() == HitResult.Type.MISS) {
                return false;
            }
            setDeltaMovement(motionNew);
            rotateTowardsMovement(1.0F);
            ricochetCount++;
            return true;
        }
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        if (!level.isClientSide()) {
            if (getOwner() instanceof SilverChariotEntity) {
                SilverChariotEntity stand = (SilverChariotEntity) getOwner();
                if (stand.isFollowingUser() && player.is(stand.getUser())) {
                    takeRapier(stand);
                }
            }
        }
    }
    
    public void takeRapier(SilverChariotEntity stand) {
        if (stand.is(getOwner()) && CommonReflection.getProjectileLeftOwner(this)) {
            stand.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
            stand.setRapier(true);
            IStandPower.getStandPowerOptional(stand.getUser()).ifPresent(power -> {
                if (ModStandsInit.SILVER_CHARIOT_RAPIER_LAUNCH.get().isUnlocked(power)) {
                    power.setCooldownTimer(ModStandsInit.SILVER_CHARIOT_RAPIER_LAUNCH.get(), 0);
                }
            });
            remove();
        }
    }
    
    @Override
    public boolean isGlowing() {
        return level.isClientSide() && getOwner() instanceof StandEntity && 
                ((StandEntity) getOwner()).getUser() == ClientUtil.getClientPlayer() || super.isCurrentlyGlowing();
    }
    
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
    
    
    private static final Vec3 OFFSET_YROT = new Vec3(0.0, -0.29, 0.375);
    private static final Vec3 OFFSET_XROT = new Vec3(0, 0.0, 1.375);
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return OFFSET_YROT;
    }
    
    @Override
    protected Vec3 getXRotOffset() {
        return OFFSET_XROT;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Ricochets", ricochetCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        ricochetCount = nbt.getInt("Ricochets");
    }

}
