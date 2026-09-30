package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class BladeHatEntity extends ItemNbtProjectileEntity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> RETURNING_TO_OWNER = SynchedEntityData.defineId(BladeHatEntity.class, EntityDataSerializers.BOOLEAN);

    public BladeHatEntity(Level world, double x, double y, double z, ItemStack thrownStack) {
        super(ModEntityTypes.BLADE_HAT.get(), world, x, y, z, thrownStack);
        this.setNoGravity(true);
        this.setBaseDamage(6.0F);
    }

    public BladeHatEntity(Level world, LivingEntity thrower, ItemStack thrownStack) {
        super(ModEntityTypes.BLADE_HAT.get(), world, thrower, thrownStack);
        this.setNoGravity(true);
        this.setBaseDamage(6.0F);
    }
    
    public BladeHatEntity(EntityType<? extends ItemNbtProjectileEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!isReturningToOwner() && shouldReturn()) {
            changeMovementAfterHit();
        }
        if (tickCount > 100) {
            setNoGravity(false);
        }
        else if (!isInGround()) {
            if (!level.isClientSide()) {
                Vec3 motionVec = this.getDeltaMovement();
                Vec3 posVec = this.position();
                Vec3 nextPosVec = posVec.add(motionVec);
                HitResult rayTraceResult = this.level.clip(new ClipContext(posVec, nextPosVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
                if (rayTraceResult.getType() == Type.BLOCK) {
                    BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();
                    Block block = level.getBlockState(blockPos).getBlock();
                    if (block == Blocks.COBWEB || block == Blocks.TRIPWIRE || block instanceof BushBlock) {
                        MCUtil.destroyBlock(level, blockPos, true, null);
                    }
                }
            }
        }
    }
    
    protected boolean shouldReturn() {
        return tickCount > 30;
    }
    
    protected void changeMovementAfterHit() {
        if (!isReturningToOwner()) {
            setDeltaMovement(getDeltaMovement().reverse());
            setReturningToOwner(true);
        }
    }
    
    @Override
    public boolean canHitEntity(Entity entity) {
        return !entity.is(getOwner()) && super.canHitEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RETURNING_TO_OWNER, false);
    }
    
    private boolean isReturningToOwner() {
        return entityData.get(RETURNING_TO_OWNER);
    }
    
    private void setReturningToOwner(boolean returnToOwner) {
        entityData.set(RETURNING_TO_OWNER, returnToOwner);
    }
    
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.BLADE_HAT_ENTITY_HIT.get();
    }
    
    @Override
    public double getBaseDamage() {
        return super.getBaseDamage() + EnchantmentHelper.getDamageBonus(thrownStack, MobType.UNDEFINED);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setReturningToOwner(compound.getBoolean("Returning"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Returning", isReturningToOwner());
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {}

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        ClientTickingSoundsHelper.playBladeHatSound(this);
    }

}
