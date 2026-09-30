package com.github.standobyte.jojo.entity;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.ai.SpecificTargetGoal;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.potion.BleedingEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class GETransformationEntity extends Entity implements IEntityAdditionalSpawnData, IPassengerMixinReposition {
    private static final EntityDataAccessor<Boolean> LIFE_FORM_SPAWNED = SynchedEntityData.defineId(GETransformationEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TURNING_BACK = SynchedEntityData.defineId(GETransformationEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REVERSE_SIGNAL = SynchedEntityData.defineId(GETransformationEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<OptionalInt> HOST_ID = SynchedEntityData.defineId(GETransformationEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    
    private GETransformationData source = new GETransformationData();
    private EntityOwnerResolver owner = new EntityOwnerResolver();
    
    private Entity target;
    
    private int duration;
    private float renderAsItemTime;
    public int actionCooldown;
    
    private EntityOwnerResolver host = new EntityOwnerResolver();
    private Vec3 hostFollowOffset = Vec3.ZERO;
    
    
    public GETransformationEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public GETransformationEntity(Level pLevel) {
        this(ModEntityTypes.GE_LIFEFORM_TRANSFORMATION.get(), pLevel);
    }
    
    public GETransformationEntity withTransformationTarget(Entity entity) {
        this.target = entity;
        return this;
    }
    
    public GETransformationEntity withOwner(LivingEntity user) {
        this.owner.setThrower(user);
        return this;
    }
    
    public GETransformationEntity withDuration(int duration) {
        this.duration = duration;
        this.renderAsItemTime = Math.min((float) duration / 3, 20);
        return this;
    }
    
    public GETransformationData getTfSourceData() {
        return source;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public boolean isTurningBack() {
        return entityData.get(IS_TURNING_BACK);
    }
    
    public Entity getTransformationTarget() {
        return target;
    }
    
    @SuppressWarnings("deprecation")
    private void turnInto() {
        Entity entityToSummon = null;
        BlockState blockToPlace = null;
        if (isTurningBack()) {
            if (source.sourceEntity != null) {
                entityToSummon = source.sourceEntity;
            }
            else if (source.sourceBlockState != null) {
                blockToPlace = source.sourceBlockState;
            }
        }
        else {
            entityToSummon = target;
        }
        BlockPos blockPos = blockPosition();
        
        if (blockToPlace != null) {
            blockToPlace = Block.updateFromNeighbourShapes(blockToPlace, level, blockPos);
            BlockState existingBlock = level.getBlockState(blockPos);
            if (!((existingBlock.isAir() || existingBlock.canBeReplaced())
                    && blockToPlace.canSurvive(level, blockPos))) {
                if (!(blockToPlace.getBlock() instanceof BaseFireBlock)) {
                    level.levelEvent(2001, blockPos, Block.getId(blockToPlace));
                }
                BlockEntity tileEntity = null;
                if (source.sourceTileEntityNbt != null) {
                    tileEntity = BlockEntity.loadStatic(blockToPlace, source.sourceTileEntityNbt);
                }
                Block.dropResources(blockToPlace, level, blockPos, tileEntity, owner.getEntity(level), ItemStack.EMPTY);
                // FIXME items in chest-like tile entities are lost
                if (tileEntity != null) {
                    if (tileEntity instanceof Container) {
                        Container inventory = (Container) tileEntity;
                        Containers.dropContents(level, blockPos, inventory);
                    }
                }
                
                blockToPlace = null;
            }
        }
        
        if (entityToSummon != null) {
            entityToSummon.copyPosition(this);
            if (entityToSummon instanceof Mob) {
                ((Mob) entityToSummon).setPersistenceRequired();
            }
            else if (entityToSummon instanceof ItemEntity) {
                ((ItemEntity) entityToSummon).setNoPickUpDelay();
            }
            copyStatus(this, entityToSummon);
            level.addFreshEntity(entityToSummon);
            if (!isTurningBack()) {
                if (entityToSummon instanceof Mob) {
                    Mob mob = (Mob) entityToSummon;
                    mob.playAmbientSound();
                    if (source.followTarget != null && source.followTargetMode != null) {
                        switch (source.followTargetMode) {
                        case AGGRO_TRACK:
                            mob.targetSelector.addGoal(0, new SpecificTargetGoal(mob, source.followTarget, false, false));
                            break;
                        case AGGRO_FORGETFUL:
                            Entity target = ((ServerLevel) level).getEntity(source.followTarget);
                            if (target instanceof LivingEntity) {
                                mob.setTarget((LivingEntity) target);
                            }
                            break;
                        default:
                            break;
                        }
                    }
                }
                hostBleeding();
            }
        }
        else if (blockToPlace != null) {
            Entity ownerEntity = owner.getEntity(level);
            if (!ForgeEventFactory.onBlockPlace(ownerEntity, BlockSnapshot.create(level.dimension(), level, blockPos.below()), Direction.UP)) {
                if (this.isOnFire()) {
                    blockToPlace.catchFire(level, blockPos, Direction.UP, null);
                }
                else {
                    level.setBlock(blockPos, blockToPlace, 3);
                    if (source.sourceTileEntityNbt != null) {
                        BlockEntity tileEntity = BlockEntity.loadStatic(blockToPlace, source.sourceTileEntityNbt);
                        if (tileEntity != null) {
                            level.setBlockEntity(blockPos, tileEntity);
                        }
                    }
                }
            }
        }
        
        discard();
    }
    
    @Override
    public void tick() {
        if (!level.isClientSide()) {
            source.resolveNbtRead(level);
        }
        else if (ClientUtil.canHearStands()) {
            clTickSound();
        }
        
        if (tickCount >= duration) {
            if (!level.isClientSide()) {
                entityData.set(LIFE_FORM_SPAWNED, true);
                turnInto();
            }
            return;
        }
        else {
            if (isTurningBack() && source.sourceEntity == null && source.sourceBlockState != null) {
                int timeLeft = duration - tickCount;
                float timeAsBlock = getRenderAsItemTime();
                if (timeLeft - 1 <= timeAsBlock && timeLeft > timeAsBlock) {
                    BlockPos blockPos = blockPosition();
                    Vec3 pos = Vec3.atBottomCenterOf(blockPos);
//                    BlockPos blockPosNew = new BlockPos(pos);
//                    if (!blockPosNew.equals(blockPos)) {
//                        BlockPos diff = blockPosNew.subtract(blockPos);
//                        pos = pos.subtract(diff.getX(), diff.getY(), diff.getZ());
//                        JojoMod.LOGGER.debug(diff);
//                    }
                    moveTo(pos);
                }
            }
        }
        
        
        double f = getEyeHeight() - 0.11111111;
        Vec3 deltaMovement = getDeltaMovement();
        if (isInWater() && getFluidHeight(FluidTags.WATER) > f) {
            setDeltaMovement(
                    deltaMovement.x * 0.99, 
                    deltaMovement.y + (deltaMovement.y < 0.06 ? 5.0E-4 : 0), 
                    deltaMovement.z * 0.99);
        }
        else if (isInLava() && getFluidHeight(FluidTags.LAVA) > f) {
            setDeltaMovement(
                    deltaMovement.x * 0.95, 
                    deltaMovement.y + (deltaMovement.y < 0.06 ? 5.0E-4 : 0), 
                    deltaMovement.z * 0.95);
        }
        else if (!isNoGravity()) {
            setDeltaMovement(deltaMovement.add(0, -0.04, 0));
        }

        deltaMovement = getDeltaMovement();
        if (!onGround || (deltaMovement).horizontalDistanceSqr() > 1.0E-5 || (tickCount + getId()) % 4 == 0) {
            move(MoverType.SELF, deltaMovement);
            double inertia = 0.98;
            if (onGround) {
                inertia = level.getBlockState(new BlockPos(getX(), getY() - 1.0, getZ()))
                        .getSlipperiness(level, new BlockPos(getX(), getY() - 1.0, getZ()), this) * 0.98;
            }
            deltaMovement = deltaMovement.multiply(inertia, 0.98, inertia);
            
            if (onGround && deltaMovement.y < 0.0D) {
                deltaMovement = deltaMovement.multiply(1.0, -0.5, 1.0);
            }
            
            setDeltaMovement(deltaMovement);
        }
        
        if (!level.isClientSide() && !isTurningBack()) {
            LivingEntity host = this.host.getEntityLiving(level);
            if (host != null && host.isAlive()) {
                if (getVehicle() != host) {
                    withHost(null);
                }
                else {
                    int lifeformCreationTick = tickCount;
                    if (lifeformCreationTick > 0) {
                        if (lifeformCreationTick > 40) {
                            hostBleeding();
                            withHost(null);
                        }
                        else if (lifeformCreationTick % 10 == 9) {
                            dealDamageToHost();
                        }
                    }
                }
            }
        }
        
        refreshDimensions();
        
        super.tick();
    }
    
    private void clTickSound() {
        SoundEvent sound = null;
        float volume = 1;
        float pitch = 1;
        if (tickCount == 1) {
            sound = isTurningBack() ? ModSounds.GOLD_EXPERIENCE_LIFE_REVERT.get() : ModSounds.GOLD_EXPERIENCE_LIFE_START.get();
        }
        
        if (sound != null) {
            level.playLocalSound(getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch, false);
        }
    }
    
    // Mojang?!?
    @Override
    public void refreshDimensions() {
        double x = getX();
        double y = getY();
        double z = getZ();
        super.refreshDimensions(); // why does it shift the entity along the XZ axes when it increases in size anyway?...
        this.setPos(x, y, z);
    }
    
    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        EntityDimensions size = new EntityDimensions(getBbWidth(), getBbHeight(), false);
        float scale = 0;
        
        float tfProgressTime = getTfProgressTime(0);
        if (tfProgressTime < renderAsItemTime) {
            Entity sourceEntity = source.getSourceEntity();
            BlockState sourceBlockState = source.getSourceBlockState();
            if (sourceEntity != null || sourceBlockState != null) {
                scale = 1 - tfProgressTime / renderAsItemTime;
                if (scale > 0) {
                    if (sourceEntity != null) {
                        size = sourceEntity.getDimensions(pPose).scale(scale);
                    }
                    else {
                        size = EntityDimensions.scalable(1, 1).scale(scale);
                    }
                }
            }
        }
        
        else if (target != null) {
            scale = 1 - (duration - tfProgressTime) / (duration - renderAsItemTime);
            if (scale > 0) {
                size = target.getDimensions(pPose).scale(scale);
            }
        }
        
        return size;
    }
    
    public float getTfProgressTime(float partialTick) {
        float time = Math.min(tickCount + partialTick, duration);
        return isTurningBack() ? duration - time : time;
    }
    
    public float getRenderAsItemTime() {
        return renderAsItemTime;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(LIFE_FORM_SPAWNED, false);
        entityData.define(IS_TURNING_BACK, false);
        entityData.define(REVERSE_SIGNAL, false);
        entityData.define(HOST_ID, OptionalInt.empty());
    }
    
    @Override
    public boolean isInvisible() {
        return super.isInvisible() || entityData.get(LIFE_FORM_SPAWNED);
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (REVERSE_SIGNAL.equals(key)) {
            if (entityData.get(REVERSE_SIGNAL)) {
                reverseTransformation();
            }
        }
        else if (HOST_ID.equals(key)) {
            updateHostEntity(entityData.get(HOST_ID));
        }
    }
    
    private void reverseTransformation() {
        int ticks = TURN_BACK_TICKS;
        if (tickCount < ticks) {
            tickCount = duration - tickCount;
        }
        else {
            float prevItemTime = getRenderAsItemTime();
            
            if (tickCount > prevItemTime) {
                renderAsItemTime = (ticks / 3F);
                duration = (int) ((duration - prevItemTime) * (ticks - renderAsItemTime)
                                    / (tickCount - prevItemTime) + renderAsItemTime);
            }
            else {
                renderAsItemTime = ticks * prevItemTime / tickCount;
                duration = Mth.ceil(renderAsItemTime);
            }
            
            tickCount = duration - ticks;
        }
        
        if (level.isClientSide() && ClientUtil.canHearStands()) {
            level.playLocalSound(getX(), getY(), getZ(), 
                    ModSounds.GOLD_EXPERIENCE_LIFE_REVERT.get(), getSoundSource(), 
                    1, 1, false);
        }
    }
    
    
    private static final int TURN_BACK_TICKS = 10;
    public static void turnEntityBack(Entity entity, GETransformationData source, @Nullable LivingEntity owner) {
        if (entity instanceof GETransformationEntity) {
            GETransformationEntity tfEntity = (GETransformationEntity) entity;
            tfEntity.entityData.set(IS_TURNING_BACK, true);
            tfEntity.entityData.set(REVERSE_SIGNAL, true);
            if (tfEntity.target instanceof LivingEntity) {
                LivingEntity turtle = (LivingEntity) tfEntity.target;
                IStandPower.getStandPowerOptional(turtle).ifPresent(turtleStand -> {
                    turtleStand.getContinuousEffects().onStandUserRemoved(turtle);
                });
            }
        }
        else {
            Level world = entity.level;
            GETransformationEntity tf = new GETransformationEntity(world)
                    .withTransformationTarget(entity)
                    .withDuration(TURN_BACK_TICKS)
                    .withOwner(owner);
            tf.entityData.set(IS_TURNING_BACK, true);
            
            if (source.sourceEntity instanceof ItemEntity) {
                ItemStack item = ((ItemEntity) source.sourceEntity).getItem();
                if (!item.isEmpty() && item.getItem() instanceof BlockItem && item.getCount() == 1) {
//                    BlockState blockToPlace = null;
//                    if (blockToPlace != null) {
//                        source.withEntitySource(null).withBlockSource(blockToPlace, null);
//                    }
                }
            }
            tf.source.copyFrom(source, world);
            
            if (tf.source.sourceBlockState != null) {
                final BlockState directionalBlock = tf.source.sourceBlockState;
                Optional<BlockState> rotated = directionalBlock.getProperties().stream()
                        .filter(property -> property instanceof DirectionProperty)
                        .findFirst()
                        .map(property -> (DirectionProperty) property)
                        .flatMap(property -> {
                            Vec3 lookVec = entity.getLookAngle();
                            Collection<Direction> possibleDirs = property.getPossibleValues();
                            return possibleDirs.stream()
                                    .max(Comparator.comparingDouble(dir -> lookVec.dot(new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ()))))
                                    .map(closestDir -> directionalBlock.setValue(property, closestDir));
                        });
                rotated.ifPresent(rotatedBlock -> tf.source.sourceBlockState = rotatedBlock);
            }
            
            copyStatus(entity, tf);
            
            Vec3 pos = entity.position();
            tf.moveTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
            entity.level.addFreshEntity(tf);
            
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                CommonReflection.dropEquipment(living);
                if (living instanceof Fox) { // i'm pretty sure this is also supposed to be in dropEquipment, and not in dropAllDeathLoot
                    ItemStack itemstack = living.getItemBySlot(EquipmentSlot.MAINHAND);
                    if (!itemstack.isEmpty()) {
                        living.spawnAtLocation(itemstack);
                        living.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }
                }
            }
            if (entity instanceof CocoJumboTurtleEntity) { // kill me
                ((CocoJumboTurtleEntity) entity).dropKey();
            }
            entity.discard();
        }
    }
    
    private static void copyStatus(Entity from, Entity to) {
        if (from.isOnFire()) {
            to.setSecondsOnFire((from.getRemainingFireTicks() + 19) / 20);
        }
        if (from.isPassenger()) {
            to.startRiding(from.getVehicle());
        }
        if (from.isVehicle()) {
            from.getPassengers().forEach(passenger -> passenger.startRiding(to));
        }
        if (from.hasCustomName() && !(to instanceof ItemEntity)) {
            to.setCustomName(from.getCustomName());
        }
        to.setDeltaMovement(from.getDeltaMovement());
    }
    
    
    public GETransformationEntity withHost(LivingEntity hostEntity) {
        if (hostEntity != null) {
            boolean riding = startRiding(hostEntity, true);
            if (riding) {
                float height = hostEntity.getBbHeight();
                hostFollowOffset = new Vec3(0, height - 0.5, 0);
            }
        }
        else if (getVehicle() == this.host.getEntity(level)) {
            stopRiding();
        }
        this.host.setThrower(hostEntity);
        if (!level.isClientSide()) {
            entityData.set(HOST_ID, hostEntity != null ? OptionalInt.of(hostEntity.getId()) : OptionalInt.empty());
        }
        return this;
    }
    
    private void updateHostEntity(OptionalInt entityId) {
        if (level.isClientSide()) {
            Entity entity = level.getEntity(entityId.orElse(-1));
            withHost(entity instanceof LivingEntity ? (LivingEntity) entity : null);
        }
    }
    
    @Override
    public boolean isPickable() {
        boolean isPickable = super.isPickable();
        if (isPickable) {
            Entity vehicle = getVehicle();
            if (vehicle != null && vehicle == host.getEntity(level)) {
                return false;
            }
        }
        return isPickable;
    }
    
    @Override
    public Vec3 repositionPassenger(Entity vehicle) {
        if (hostFollowOffset != null && vehicle == host.getEntity(level)) {
            return vehicle.position().add(hostFollowOffset);
        }
        return null;
    }
    
    private void hostBleeding() {
        if (!level.isClientSide()) {
            LivingEntity host = this.host.getEntityLiving(level);
            if (host != null && host.isAlive()) {
                if (hostFollowOffset != null) {
                    BleedingEffect.setNextParticlesPos(host, host.position().add(hostFollowOffset).add(0, 0.5, 0));
                }
                host.addEffect(new MobEffectInstance(ModStatusEffects.BLEEDING.get(), 200, 1, false, false, true));
            }
        }
    }
    
    private void dealDamageToHost() {
        if (!level.isClientSide()) {
            LivingEntity host = this.host.getEntityLiving(level);
            if (host != null && host.isAlive()) {
                DamageUtil.hurtThroughInvulTicks(host, ModDamageTypes.source(host, "arrowLifeform"), 2);
            }
        }
    }
    
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.tickCount = nbt.getInt("Age");
        withDuration(nbt.getInt("Duration"));
        entityData.set(IS_TURNING_BACK, nbt.getBoolean("TurnBack"));
        actionCooldown = nbt.getInt("ActionCD");
        
        source.readNbt(nbt);
        if (nbt.contains("TargetEntity", MCUtil.getNbtId(CompoundTag.class))) {
            CompoundTag entityNbt = nbt.getCompound("TargetEntity");
            target = EntityType.create(entityNbt, level).orElse(null);
        }
        owner.loadNbt(nbt, "Owner");
        
        host.loadNbt(nbt, "Host");
        hostFollowOffset = MCUtil.nbtGetVec3d(nbt, "HostOffset");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("Duration", duration);
        nbt.putBoolean("TurnBack", entityData.get(IS_TURNING_BACK));
        nbt.putInt("ActionCD", actionCooldown);
        
        source.writeNbt(nbt);
        if (target != null) {
            CompoundTag entityNbt = target.serializeNBT();
            nbt.put("TargetEntity", entityNbt);
        }
        owner.saveNbt(nbt, "Owner");
        
        host.saveNbt(nbt, "Host");
        if (hostFollowOffset != null) {
            MCUtil.nbtPutVec3d(nbt, "HostOffset", hostFollowOffset);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(tickCount);
        buffer.writeVarInt(duration);
        owner.writeNetwork(buffer);
        
        writeEntityData(buffer, target);
        source.resolveNbtRead(level);
        source.toBuf(buffer);
        
        host.writeNetwork(buffer);
        NetworkUtil.writeOptionally(buffer, hostFollowOffset, (vec, buf) -> NetworkUtil.writeVecApproximate(buf, vec));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        tickCount = additionalData.readVarInt();
        withDuration(additionalData.readVarInt());
        owner.readNetwork(additionalData);
        
        target = readEntityData(additionalData, level);
        source.fromBuf(additionalData, level);
        
        host.readNetwork(additionalData);
        hostFollowOffset = NetworkUtil.readOptional(additionalData, NetworkUtil::readVecApproximate).orElse(null);
    }
    
    
    
    public enum FollowTargetMode {
        TRACK,
        AGGRO_TRACK,
        AGGRO_FORGETFUL,
        DELIVERY
    }
    
    public static class GETransformationData {
        private UUID followTarget;
        private FollowTargetMode followTargetMode;
        private Entity sourceEntity;
        private CompoundTag sourceEntityNbt = null;
        private BlockState sourceBlockState;
        private BlockPos sourceBlockPos;
        private CompoundTag sourceTileEntityNbt = null;
        
        
        
        public GETransformationData withEntitySource(Entity entity) {
            this.sourceEntity = entity;
            return this;
        }
        
        public GETransformationData withBlockSource(BlockState blockState, BlockPos blockPos, @Nullable BlockEntity tileEntity) {
            this.sourceBlockState = blockState;
            this.sourceBlockPos = blockPos;
            if (tileEntity != null) {
                sourceTileEntityNbt = tileEntity.save(new CompoundTag());
            }
            return this;
        }
        
        public GETransformationData withFollowTarget(UUID entity, FollowTargetMode mode, LivingEntity standUser) {
            if (standUser != null && standUser.getUUID().equals(entity)) {
                switch (mode) {
                case AGGRO_TRACK:
                    mode = FollowTargetMode.TRACK;
                    break;
                case AGGRO_FORGETFUL:
                    mode = null;
                    break;
                default:
                    break;
                }
            }
            this.followTarget = mode != null ? entity : null;
            this.followTargetMode = mode;
            return this;
        }
        
        public void copyFrom(GETransformationData other, Level world) {
            this.followTarget = other.followTarget;
            this.sourceEntity = other.sourceEntity;
            this.sourceBlockState = other.sourceBlockState;
            this.sourceBlockPos = other.sourceBlockPos;
            this.sourceTileEntityNbt = other.sourceTileEntityNbt;
        }
        
        
        /**
         * Call this during tick or before sending the data from server.
         */
        public void resolveNbtRead(Level world) {
            if (sourceEntityNbt != null) {
                withEntitySource(EntityType.create(sourceEntityNbt, world).orElse(null));
                sourceEntityNbt = null;
            }
        }
        
        
        
        public List<MobEffectInstance> getItemEffects() {
            ItemStack item = ItemStack.EMPTY;
            if (sourceEntity instanceof ItemEntity) {
                item = ((ItemEntity) sourceEntity).getItem();
            }
            else if (sourceEntity instanceof ThrownPotion) {
                item = MCUtil.getItemOnServer((ThrownPotion) sourceEntity);
            }
            return !item.isEmpty() && item.hasTag() ? PotionUtils.getMobEffects(item) : Collections.emptyList();
        }
        
        public ItemStack clMakeSourceItemView() {
            if (sourceEntity != null) {
                if (sourceEntity instanceof ItemEntity) {
                    return ((ItemEntity) sourceEntity).getItem().copy();
                }
                else if (sourceEntity instanceof ItemSupplier) {
                    return ((ItemSupplier) sourceEntity).getItem().copy();
                }
                else if (sourceEntity instanceof PrimedTnt) {
                    return new ItemStack(Items.TNT);
                }
                else if (sourceEntity.getType() == ModEntityTypes.ROAD_ROLLER.get()) {
                    return new ItemStack(ModItems.ROAD_ROLLER.get());
                }
                else if (sourceEntity.getType() == EntityType.END_CRYSTAL) {
                    return new ItemStack(Items.END_CRYSTAL);
                }
                else if (sourceEntity instanceof Boat) {
                    return new ItemStack(((Boat) sourceEntity).getDropItem());
                }
            }
            else if (sourceBlockState != null) {
                Block block = sourceBlockState.getBlock();
                Item blockItem = block.asItem();
                if (blockItem != null && blockItem != Items.AIR) {
                    return new ItemStack(blockItem);
                }
            }
            
            return ItemStack.EMPTY;
        }
        
        public MutableComponent clMakeSourceName() {
            if (sourceEntity != null) {
                Component name = sourceEntity.getDisplayName();
                if (name instanceof MutableComponent) {
                    return (MutableComponent) name;
                }
                throw new ClassCastException("Why do ITextComponent and IFormattableTextComponent interfaces both exist? Why not just make ITextComponent formattable? Separating them doesn't even do shit, ffs OOP was a mistake");
            }
            
            else if (sourceBlockState != null) {
                return sourceBlockState.getBlock().getName(); // oh, look, this returns IFormattableTextComponent!
            }
            
            return (Component) Component.empty();
        }
        
        
        @Nullable
        public Entity getSourceEntity() {
            return sourceEntity;
        }
        
        @Nullable
        public BlockState getSourceBlockState() {
            return sourceBlockState;
        }
        
        @Nullable
        public BlockPos getSourceBlockPos() {
            return sourceBlockPos;
        }
        
        @Nullable
        public UUID getFollowTarget() {
            return followTarget;
        }
        
        public FollowTargetMode getFollowTargetMode() {
            return followTargetMode;
        }
        
        
        
        public void writeNbt(CompoundTag nbt) {
            if (sourceEntity != null) {
                CompoundTag entityNbt = sourceEntity.serializeNBT();
                nbt.put("GESourceEntity", entityNbt);
            }
            if (sourceBlockState != null) {
                nbt.put("GESourceBlock", NbtUtils.writeBlockState(sourceBlockState));
            }
            if (sourceBlockPos != null) {
                nbt.put("GESourcePos", NbtUtils.writeBlockPos(sourceBlockPos));
            }
            if (sourceTileEntityNbt != null) {
                nbt.put("GESourceTE", sourceTileEntityNbt);
            }
            if (followTarget != null) {
                nbt.putUUID("Owner", followTarget);
                if (followTargetMode != null) {
                    MCUtil.nbtPutEnum(nbt, "FollowMode", followTargetMode);
                }
            }
        }
        
        public void readNbt(CompoundTag nbt) {
            if (nbt.contains("GESourceEntity", MCUtil.getNbtId(CompoundTag.class))) {
                sourceEntityNbt = nbt.getCompound("GESourceEntity");
            }
            if (nbt.contains("GESourceBlock", MCUtil.getNbtId(CompoundTag.class))) {
                sourceBlockState = NbtUtils.readBlockState(nbt.getCompound("GESourceBlock"));
            }
            if (nbt.contains("GESourcePos", MCUtil.getNbtId(CompoundTag.class))) {
                sourceBlockPos = NbtUtils.readBlockPos(nbt.getCompound("GESourcePos"));
            }
            if (nbt.contains("GESourceTE", MCUtil.getNbtId(CompoundTag.class))) {
                sourceTileEntityNbt = nbt.getCompound("GESourceTE");
            }
            if (nbt.hasUUID("Owner")) {
                followTarget = nbt.getUUID("Owner");
                followTargetMode = MCUtil.nbtGetEnum(nbt, "FollowMode", FollowTargetMode.class);
            }
        }
        
        public void toBuf(FriendlyByteBuf buffer) {
            writeEntityData(buffer, sourceEntity);
            NetworkUtil.writeOptionally(buffer, sourceBlockState, blockState -> NetworkUtil.writeBlockState(buffer, blockState));
            NetworkUtil.writeOptionally(buffer, sourceBlockPos, blockPos -> buffer.writeBlockPos(blockPos));
        }
        
        public void fromBuf(FriendlyByteBuf buffer, Level world) {
            sourceEntity = readEntityData(buffer, world);
            sourceBlockState = NetworkUtil.readOptional(buffer, () -> NetworkUtil.readBlockState(buffer)).orElse(null);
            sourceBlockPos = NetworkUtil.readOptional(buffer, () -> buffer.readBlockPos()).orElse(null);
        }
    }
    
    
    
    private static void writeEntityData(FriendlyByteBuf buffer, Entity entityToWrite) {
        NetworkUtil.writeOptionally(buffer, entityToWrite, entity -> {
            byte pitch = (byte) Mth.floor(entity.xRot * 256.0F / 360.0F);
            byte yaw = (byte) Mth.floor(entity.yRot * 256.0F / 360.0F);
            byte headYaw = (byte) (entity.getYHeadRot() * 256.0F / 360.0F);
            
            buffer.writeRegistryId(entity.getType());
            buffer.writeByte(pitch);
            buffer.writeByte(yaw);
            buffer.writeByte(headYaw);
            
            if (entity instanceof IEntityAdditionalSpawnData) {
                ((IEntityAdditionalSpawnData) entity).writeSpawnData(buffer);
            }
            
            List<SynchedEntityData.DataValue<?>> entityData = entity.getEntityData().getAll();
            try {
                SynchedEntityData.pack(entityData, buffer);
            } catch (IOException e) {
                JojoMod.getLogger().error("Failed to write entity data for Gold Experience's transformation render for entity of type {}", MCUtil.id(entity.getType()));
                e.printStackTrace();
            }
        });
    }
    
    private static Entity readEntityData(FriendlyByteBuf buffer, Level world) {
        return NetworkUtil.readOptional(buffer, () -> {
            EntityType<?> type = buffer.readRegistryIdSafe(EntityType.class);
            Entity entity = type.create(world);
            
            float pitch = (buffer.readByte() * 360) / 256.0F;
            float yaw = (buffer.readByte() * 360) / 256.0F;
            float headYaw = (buffer.readByte() * 360) / 256.0F;
            
            entity.yRot = yaw % 360.0F;
            entity.xRot = Mth.clamp(pitch, -90.0F, 90.0F) % 360.0F;
            entity.yRotO = entity.yRot;
            entity.xRotO = entity.xRot;
            entity.setYHeadRot(headYaw);
            entity.setYBodyRot(headYaw);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.yHeadRotO = living.yHeadRot;
                living.yBodyRotO = living.yBodyRot;
            }

            if (entity instanceof IEntityAdditionalSpawnData) {
                ((IEntityAdditionalSpawnData) entity).readSpawnData(buffer);
            }
            
            try {
                List<SynchedEntityData.DataValue<?>> entityData = SynchedEntityData.unpack(buffer);
                entity.getEntityData().assignValues(entityData);
            } catch (IOException e) {
                JojoMod.getLogger().error("Failed to read entity data for Gold Experience's transformation render for entity of type {}", MCUtil.id(entity.getType()));
                e.printStackTrace();
            } catch (Exception e) {
                JojoMod.getLogger().error("Failed to assign entity data for Gold Experience's transformation render for entity of type {}", MCUtil.id(entity.getType()));
                e.printStackTrace();
            }
            
            return entity;
        }).orElse(null);
    }

}