package com.github.standobyte.jojo.entity.damaging;

import com.github.standobyte.jojo.util.mc.damage.ModDamageTypes;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.mc.damage.IndirectStandEntityDamageSource;
import com.github.standobyte.jojo.util.mc.damage.ModdedDamageSourceWrapper;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.scores.Team;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public abstract class DamagingEntity extends Projectile implements IEntityAdditionalSpawnData {
    protected static final Vec3 DEFAULT_POS_OFFSET = new Vec3(0.0D, -0.3D, 0.0D);
    private float damageFactor = 1F;
    // only used for OwnerBoundProjectileEntity
    protected double speedFactor = 1F;
    private LivingEntity livingEntityOwner = null;
    private LivingEntity powerUser = null;
    private LazyOptional<IStandPower> userStandPower = LazyOptional.empty();
    private LazyOptional<INonStandPower> userNonStandPower = LazyOptional.empty();
    private Optional<ResourceLocation> standSkin = Optional.empty();

    public DamagingEntity(EntityType<? extends DamagingEntity> entityType, @Nullable LivingEntity owner, Level world) {
        this(entityType, world);
        if (owner != null) {
            setOwner(owner);
            setLivingOwner(owner);
            Vec3 pos = getPos(owner, 1.0F, owner.yRot, owner.xRot);
            setPos(pos.x, pos.y, pos.z);
            setRot(owner.yRot, owner.xRot);
        }
    }

    public DamagingEntity(EntityType<? extends DamagingEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    public void setShootingPosOf(LivingEntity entity) {
        Vec3 pos = getPos(entity, 1.0F, entity.yRot, entity.xRot);
        setPos(pos.x, pos.y, pos.z);
        setRot(entity.yRot, entity.xRot);
    }
    
    public void withStandSkin(Optional<ResourceLocation> standSkin) {
        this.standSkin = standSkin;
    }
    
    protected final Vec3 getPos(LivingEntity owner, float partialTick, float yRot, float xRot) {
        return owner.getEyePosition(partialTick)
                .add(getOwnerRelativeOffset().add(
                        getXRotOffset().xRot(-owner.xRot * MathUtil.DEG_TO_RAD))
                        .yRot(-yRot * MathUtil.DEG_TO_RAD));
    }
    
    protected Vec3 getOwnerRelativeOffset() {
        return DEFAULT_POS_OFFSET;
    }
    
    protected Vec3 getXRotOffset() {
        return Vec3.ZERO;
    }
    
    @Override
    public LivingEntity getOwner() {
        if (livingEntityOwner == null) {
            Entity owner = super.getOwner();
            if (owner == null) {
                return null;
            }
            if (owner instanceof LivingEntity) {
                setLivingOwner((LivingEntity) owner);
            }
        }
        return livingEntityOwner;
    }
    
    private void setLivingOwner(LivingEntity entity) {
        this.livingEntityOwner = entity;
        this.powerUser = StandUtil.getStandUser(entity);
    }
    
    @Override
    public void setOwner(Entity owner) {
        super.setOwner(owner);
        userStandPower = LazyOptional.empty();
        userNonStandPower = LazyOptional.empty();
    }
    
    protected LazyOptional<IStandPower> getUserStandPower() {
        if (!userStandPower.isPresent()) {
            userStandPower = IStandPower.getStandPowerOptional(powerUser);
        }
        return userStandPower;
    }
    
    protected LazyOptional<INonStandPower> getUserNonStandPower() {
        if (!userNonStandPower.isPresent()) {
            userNonStandPower = INonStandPower.getNonStandPowerOptional(powerUser);
        }
        return userNonStandPower;
    }
    
    @Override
    public void tick() {
        super.tick();
        checkInsideBlocks();
        checkHit();
    }
    
    protected void checkHit() {
        HitResult[] rayTrace = rayTrace();
        for (HitResult result : rayTrace) {
            if (result.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, result)) {
                onHit(result);
            }
        }
    }

    protected HitResult[] rayTrace() {
        return new HitResult[] { ProjectileUtil.getHitResult(this, this::canHitEntity) };
    }
    
    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        if (!level.isClientSide() && isAlive()) {
            Entity target = entityRayTraceResult.getEntity();
            LivingEntity owner = getOwner();
            boolean entityHurt = hurtTarget(target, owner);
            int prevTargetFireTimer = target.getRemainingFireTicks();
            if (isOnFire()) {
                target.setSecondsOnFire(5);
            }
            if (entityHurt) {
                if (owner instanceof StandEntity && target instanceof LivingEntity) {
                    LivingEntity standUser = ((StandEntity) owner).getUser();
                    if (standUser != null) {
                        LivingEntity livingTarget = (LivingEntity) target;
                        if (standUser instanceof Player) {
                            livingTarget.setLastHurtByPlayer((Player) standUser);
                            livingTarget.lastHurtByPlayerTime = 100;
                        }
                        livingTarget.setLastHurtByMob(standUser);
                    }
                }
            }
            else {
                target.setRemainingFireTicks(prevTargetFireTimer);
            }
            afterEntityHit(entityRayTraceResult, entityHurt);
        }
        super.onHitEntity(entityRayTraceResult);
    }
    
    protected boolean checkPvpRules() {
        return true;
    }
    
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        return hurtTarget(target, getDamageSource(owner), getDamageAmount());
    }
    
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float dmgAmount) {
        return DamageUtil.hurtThroughInvulTicks(target, DamageUtil.enderDragonDamageHack(dmgSource, target), dmgAmount);
    }
    
    protected DamageSource getDamageSource(LivingEntity owner) { // TODO damage sources/death messages
        DamageSource damageSource;
        if (standDamage() && owner != null) {
            damageSource = new IndirectStandEntityDamageSource("arrow", this, owner);
        }
        else {
            damageSource = ModDamageTypes.source(this, owner, ModDamageTypes.key("arrow"));
        }
        
        float knockbackReduction = knockbackMultiplier();
        if (knockbackReduction < 1) {
            if (!(damageSource instanceof IModdedDamageSource)) {
                damageSource = new ModdedDamageSourceWrapper(damageSource);
            }
            ((IModdedDamageSource) damageSource).setKnockbackReduction(Math.max(knockbackReduction, 0));
        }
        
        return damageSource;
    }
    
    protected float knockbackMultiplier() {
        return 1F;
    }
    
    protected void afterEntityHit(EntityHitResult entityRayTraceResult, boolean entityHurt) {}

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                return true;
            }
            if (entity instanceof LivingEntity) {
                if (entity.is(owner) || owner instanceof StandEntity && entity.is(((StandEntity) owner).getUser())) {
                    return canHitOwner();
                }
                else {
                    return owner.canAttack((LivingEntity) entity);
                }
            }
            return !(checkPvpRules() && 
                    owner instanceof StandEntity && !((StandEntity) owner).canHarm(entity) || 
                    owner instanceof Player && entity instanceof Player && !((Player) owner).canHarmPlayer((Player) entity));
        }
        return false;
    }
    
    public boolean canHitOwner() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockRayTraceResult) {
        super.onHitBlock(blockRayTraceResult);
        if (!level.isClientSide() && isAlive()) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos();
            LivingEntity owner = getOwner();
            boolean brokenBlock = owner != null && !JojoModUtil.canEntityDestroy((ServerLevel) level, blockPos, level.getBlockState(blockPos), owner) ? 
                    false
                    : destroyBlock(blockRayTraceResult);
            afterBlockHit(blockRayTraceResult, brokenBlock);
        }
    }
    
    protected boolean destroyBlock(BlockHitResult blockRayTraceResult) {
        BlockPos blockPos = blockRayTraceResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        Direction face = blockRayTraceResult.getDirection();
        boolean brokenBlock = canBreakBlock(blockPos, blockState);
        if (isFiery() && blockState.isFlammable(level, blockPos, face)) {
            MCUtil.blockCatchFire(level, blockPos, blockState, face, getOwner());
            return false;
        }
        if (brokenBlock) {
            LivingEntity ownerOrStandUser = getOwner();
            if (ownerOrStandUser instanceof StandEntity) {
                ownerOrStandUser = ((StandEntity) ownerOrStandUser).getUser();
            }
            boolean dropItem = ownerOrStandUser instanceof Player ? !((Player) ownerOrStandUser).abilities.instabuild : true;
            brokenBlock = MCUtil.destroyBlock(level, blockPos, dropItem, getOwner());
        }
        return brokenBlock;
    }
    
    protected boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        float hardness = blockState.getDestroySpeed(level, blockPos);
        return hardness >= 0 && hardness <= getMaxHardnessBreakable();
    }
    
    protected void afterBlockHit(BlockHitResult blockRayTraceResult, boolean blockDestroyed) {}
    
    public boolean isFiery() {
        return false;
    }
    
    public abstract int ticksLifespan();

    protected abstract float getBaseDamage();
    
    protected float getDamageAmount() {
        float configMultiplier = standDamage() || getOwner() instanceof StandEntity ? JojoModConfig.getCommonConfigInstance(false).standDamageMultiplier.get().floatValue() : 1;
        float damage = getBaseDamage() * configMultiplier;
        if (debuffsFromStand()) {
            damage *= damageFactor;
        }
        return damage;
    }
    
    protected float getDamageFinalCalc(float damage) {
        return damage;
    }
    
    public void setDamageFactor(float damageFactor) {
        this.damageFactor = damageFactor;
    }
    
    public float getDamageFactor() {
        return damageFactor;
    }
    
    public void setSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }
    
    public double getSpeedFactor() {
        return speedFactor;
    }
    
    protected boolean debuffsFromStand() {
        return true;
    }
    
    protected abstract float getMaxHardnessBreakable();
    
    public abstract boolean standDamage();
    
    protected boolean standVisibility() {
        return standDamage();
    }

    @Override
    public boolean isInvisible() {
        return standVisibility() || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return standVisibility() && !StandUtil.clStandEntityVisibleTo(player) 
                || !JojoModUtil.seesInvisibleAsSpectator(player) && super.isInvisible();
    }
    
    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!this.isSilent()) {
            if (standVisibility()) {
                MCUtil.playSound(level, null, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch, StandUtil::playerCanHearStands);
            }
            else {
                level.playSound(null, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch);
            }
        }
    }
    
    @Override
    public boolean displayFireAnimation() {
        return super.displayFireAnimation() && (!isInvisible() || !isInvisibleTo(ClientUtil.getClientPlayer()));
    }
    
    @Override
    public Team getTeam() {
        LivingEntity owner = getOwner();
        return owner == null ? super.getTeam() : owner.getTeam();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (standDamage() && !(source instanceof IStandDamageSource)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        Vec3 pos = position();
        this.xo = pos.x;
        this.yo = pos.y;
        this.zo = pos.z;
        this.xOld = pos.x;
        this.yOld = pos.y;
        this.zOld = pos.z;
        setPosRaw(x, y, z);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        this.yRot = yRot;
        this.xRot = xRot;
        this.reapplyPosition();
    }
    
    public Optional<ResourceLocation> getStandSkin() {
        return standSkin;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("DamageFactor", damageFactor);
        nbt.putDouble("SpeedFactor", speedFactor);
        nbt.putInt("Age", tickCount);
        standSkin.ifPresent(path -> nbt.putString("StandSkin", path.toString()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        damageFactor = nbt.getFloat("DamageFactor");
        speedFactor = nbt.getDouble("SpeedFactor");
        tickCount = nbt.getInt("Age");
        standSkin = MCUtil.getNbtElement(nbt, "StandSkin", StringTag.class)
                .map(StringTag::getAsString)
                .map(ResourceLocation::new);
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(tickCount);
        buffer.writeDouble(speedFactor);
        NetworkUtil.writeOptional(buffer, standSkin, buffer::writeResourceLocation);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        tickCount = additionalData.readInt();
        speedFactor = additionalData.readDouble();
        standSkin = NetworkUtil.readOptional(additionalData, FriendlyByteBuf::readResourceLocation);
    }
}
