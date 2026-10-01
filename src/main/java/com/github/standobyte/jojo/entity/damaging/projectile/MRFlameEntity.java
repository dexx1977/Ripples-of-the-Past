package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.util.mc.MCUtil;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.ForgeEventFactory;

public class MRFlameEntity extends ModdedProjectileEntity {
    private Vec3 startingPos = null;
    
    public MRFlameEntity(LivingEntity shooter, Level world) {
        super(ModEntityTypes.MR_FLAME.get(), shooter, world);
    }
    
    protected MRFlameEntity(EntityType<? extends MRFlameEntity> type, LivingEntity shooter, Level world) {
        super(type, shooter, world);
    }

    public MRFlameEntity(EntityType<? extends MRFlameEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 1.0F;
    }
    
    @Override
    protected float knockbackMultiplier() {
        return 0.1F;
    }
    
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        startingPos = position();
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.hurtTarget(entity, owner), 10, true);
    }

    @Override
    protected HitResult[] rayTrace() {
        return new HitResult[] { JojoModUtil.getHitResult(this, this::canHitEntity, ClipContext.Block.OUTLINE) };
    }
    
    @Override
    protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, getOwner())) {
                BlockPos blockPos = blockRayTraceResult.getBlockPos();
                BlockState blockState = level.getBlockState(blockPos);
                if (!meltIceAndSnow(level, blockState, blockPos) && 
                        blockState.getCollisionShape(level, blockPos) != Shapes.empty()) {
                    blockPos = blockPos.relative(blockRayTraceResult.getDirection());
                    if (level.isEmptyBlock(blockPos)) {
                        level.setBlockAndUpdate(blockPos, ModBlocks.MAGICIANS_RED_FIRE.get().getStateForPlacement(level, blockPos));
                    }
                }
            }
        }
    }
    
    public static boolean meltIceAndSnow(Level world, BlockState blockState, BlockPos blockPos) {
        if (world.isClientSide()) return false;
        if (MCUtil.isSnowOrIce(blockState)) {
            if (world.dimensionType().ultraWarm() || !blockState.isCollisionShapeFullBlock(world, blockPos)) {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, blockState, 
                        Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
                world.removeBlock(blockPos, false);
            }
            else {
                world.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
                world.neighborChanged(blockPos, Blocks.WATER, blockPos);
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
        if (targetType == TargetType.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitTarget;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            if (!blockState.isCollisionShapeFullBlock(level, blockPos)) return;
        }
        super.breakProjectile(targetType, hitTarget);
    }

    @Override
    protected boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        return super.canBreakBlock(blockPos, blockState) && !(blockState.getBlock() instanceof BaseFireBlock);
    }
    
    @Override
    protected DamageSource getDamageSource(LivingEntity owner) {
        // the fire behaviour lives in the damage type (jojo:flame is in the
        // vanilla is_fire tag and keeps the arrow death message)
        return com.github.standobyte.jojo.util.mc.damage.ModDamageTypes.source(this, owner, 
                com.github.standobyte.jojo.util.mc.damage.ModDamageTypes.key("flame"));
    }

    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
        }
        else {
            super.tick();
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerLevel) level);
        }
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean isFiery() {
        return true;
    }
    
    @Override
    public boolean canBeEvaded(@Nullable Entity context) {
        return false;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    public int ticksLifespan() {
        return 8;
    }
    
    @Override
    protected Vec3 getOwnerRelativeOffset() {
        return Vec3.ZERO;
    }
    
    private static final Vec3 OFFSET_XROT = new Vec3(0, 0.2, 0.0);
    @Override
    protected Vec3 getXRotOffset() {
        return OFFSET_XROT;
    }
    
    public Vec3 getStartingPos() {
        return startingPos;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        Vec3 pos = startingPos != null ? startingPos : position();
        buffer.writeBoolean(true);
        buffer.writeDouble(pos.x);
        buffer.writeDouble(pos.y);
        buffer.writeDouble(pos.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        if (additionalData.readBoolean()) {
            startingPos = new Vec3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        }
        else {
            startingPos = position();
        }
    }
}
