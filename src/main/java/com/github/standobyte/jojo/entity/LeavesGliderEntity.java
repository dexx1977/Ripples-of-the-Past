package com.github.standobyte.jojo.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClLeavesGliderColorPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.CollisionUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.GameData;

public class LeavesGliderEntity extends Entity implements IEntityAdditionalSpawnData, IHasHealth, EntityMadeFromBlock {
    private static final double GRAVITY = -0.01D;
    public static final float MAX_ENERGY = 200;
    private static final float MAX_HEALTH = 4F;
    private static final int MAX_PASSENGERS = 4;

    private static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(LeavesGliderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> ENERGY = SynchedEntityData.defineId(LeavesGliderEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(LeavesGliderEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> HAMON_USERS_CHARGING = SynchedEntityData.defineId(LeavesGliderEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<BlockPos>> CRAZY_D_RESTORE = SynchedEntityData.defineId(LeavesGliderEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    
    private BlockState leavesBlock = Blocks.OAK_LEAVES.defaultBlockState();
    private ResourceLocation leavesBlockTex = null;
    private int foliageColor = -1;
    private List<INonStandPower> passengerPowers = new ArrayList<>();
    private float passengersHeight;

    private float yRotDelta;
    private boolean inputLeft;
    private boolean inputRight;
  
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    public LeavesGliderEntity(Level world) {
        this(ModEntityTypes.LEAVES_GLIDER.get(), world);
    }

    public LeavesGliderEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        tickLerp();

        if (!level.isClientSide()) {
            updateFlying();
        }
        moveGlider();
        
        if (!level.isClientSide()) {
            tickCrazyDFlag();
            rechargeFromHamonUsers();
            
            if (getEnergy() <= 0) {
                setHealth(getHealth() - 0.04F);
            }
            else {
                setHealth(Math.min(getHealth() + 0.1F, MAX_HEALTH));
            }
            if (getHealth() <= 0) {
                discard();
            }
        }
        else {
            float energy = getEnergy();
            if (energy > 0) {
                boolean[] chargingHamonUsers = getHamonChargers();
                boolean isBeingCharged = false;
                for (int i = 0; i < MAX_PASSENGERS; i++) {
                    if (chargingHamonUsers[i] && i < getPassengers().size()) {
                        Entity charger = getPassengers().get(i);
                        if (charger != null && charger.isAlive() && charger instanceof LivingEntity) {
                            CustomParticlesHelper.createHamonGliderChargeParticles((LivingEntity) charger);
                            isBeingCharged = true;
                        }
                        
                    }
                }
                
                float energyRatio = energy / MAX_ENERGY;
                Vec3 soundPos = clSoundPos();
                if (isBeingCharged || random.nextFloat() < energyRatio * 0.2F) {
                    HamonSparksLoopSound.playSparkSound(this, soundPos, energyRatio);
                    CustomParticlesHelper.createHamonSparkParticles(this, this.getRandomX(0.5F), this.getY(1.0F), this.getRandomZ(0.5F), 
                            MathUtil.fractionRandomInc(energyRatio * 2));
                }
            }
            
            for (Entity passenger : getPassengers()) { 
                if (passenger instanceof LocalPlayer) {
                    ClientReflection.setHandsBusy((LocalPlayer) passenger, true);
                }
            }
        }
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (level.isClientSide()) {
            Vec3 soundPos = clSoundPos();
            SoundType soundType = leavesBlock.getSoundType();
            level.playLocalSound(soundPos.x, soundPos.y, soundPos.z, 
                    soundType.getBreakSound(), getSoundSource(), 
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            
            addLeavesParticles(200);
        }
    }
    
    private void rechargeFromHamonUsers() {
        boolean[] hamonUsersCharging = new boolean[MAX_PASSENGERS];
        
        Iterator<INonStandPower> iter = passengerPowers.iterator();
        boolean infiniteEnergy = false;
        while (iter.hasNext()) {
            INonStandPower power = iter.next();
            if (power.getType() != ModPowers.HAMON.get()) {
                iter.remove();
            }
            else if (power.isUserCreative()) {
                infiniteEnergy = true;
                addPassengerIndex(hamonUsersCharging, power.getUser());
            }
        }
        if (infiniteEnergy) {
            setEnergy(MAX_ENERGY);
        }
        else {
            setEnergy(Math.max(getEnergy() - 2, 0));
            float energyToReplenish = MAX_ENERGY - getEnergy();
            int hamonUsersWithEnergy = passengerPowers.size();
            while (energyToReplenish > 0 && hamonUsersWithEnergy > 0) {
                float energyFromEach = energyToReplenish / hamonUsersWithEnergy;
                for (INonStandPower power : passengerPowers) {
                    float energyConsumed = consumeEnergy(power, energyFromEach);
                    if (energyConsumed > 0) {
                        addPassengerIndex(hamonUsersCharging, power.getUser());
                    }
                    if (energyConsumed < energyFromEach) {
                        hamonUsersWithEnergy--;
                    }
                    energyToReplenish -= energyConsumed;
                }
            }
            setEnergy(MAX_ENERGY - energyToReplenish);
        }
        
        setHamonChargers(hamonUsersCharging);
    }
    
    private float consumeEnergy(INonStandPower power, float energy) {
        energy = Math.min(energy, power.getEnergy());
        power.getTypeSpecificData(ModPowers.HAMON.get()).get().hamonPointsFromAction(HamonStat.CONTROL, energy);
        power.consumeEnergy(energy);
        return energy;
    }
    
    private void addPassengerIndex(boolean[] arr, Entity passenger) {
        int index = getPassengers().indexOf(passenger);
        if (index >= 0) {
            arr[index] = true;
        }
    }
    
    private void setHamonChargers(boolean[] passengerIndices) {
        byte data = 0;
        for (int i = MAX_PASSENGERS - 1; i >= 0; i--) {
            data <<= 1;
            if (passengerIndices[i]) {
                data |= 1;
            }
        }
        entityData.set(HAMON_USERS_CHARGING, data);
    }
    
    private boolean[] getHamonChargers() {
        boolean[] passengerIndices = new boolean[MAX_PASSENGERS];
        byte data = entityData.get(HAMON_USERS_CHARGING);
        for (int i = 0; i < MAX_PASSENGERS; i++) {
            passengerIndices[i] = (data & 1) > 0;
            data >>= 1;
        }
        return passengerIndices;
    }

    private void tickLerp() {
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            setPacketCoordinates(getX(), getY(), getZ());
        }
        if (lerpSteps > 0) {
            double xLerp = getX() + (lerpX - getX()) / (double) lerpSteps;
            double yLerp = getY() + (lerpY - getY()) / (double) lerpSteps;
            double zLerp = getZ() + (lerpZ - getZ()) / (double) lerpSteps;
            double yRotLerp = Mth.wrapDegrees(lerpYRot - (double) yRot);
            yRot = (float) ((double) yRot + yRotLerp / (double) lerpSteps);
            xRot = (float) ((double) xRot + (lerpXRot - (double) xRot) / (double) lerpSteps);
            --lerpSteps;
            setPos(xLerp, yLerp, zLerp);
            setRot(yRot, xRot);
        }
    }

    @Override
    public void lerpTo(double lerpX, double lerpY, double lerpZ, float lerpYRot, float lerpZRot, int lerpSteps, boolean teleport) {
        this.lerpX = lerpX;
        this.lerpY = lerpY;
        this.lerpZ = lerpZ;
        this.lerpYRot = (double) lerpYRot;
        this.lerpXRot = (double) lerpZRot;
        this.lerpSteps = 10;
    }
    
    private void updateFlying() {
        boolean prevIsFlying = isFlying();
        boolean isFlying = !onGround() && !isInWaterOrBubble();
        if (prevIsFlying && !isFlying) {
            setDeltaMovement(Vec3.ZERO);
            if (!level.isClientSide()) {
                ejectPassengers();
            }
        }
        setIsFlying(isFlying);
    }
    
    private void moveGlider() {
        Optional<BlockPos> crazyDRestore = entityData.get(CRAZY_D_RESTORE);
        if (crazyDRestore.isPresent()) {
            CrazyDiamondHeal.addParticlesAround(this);
            setDeltaMovement(Vec3.atCenterOf(crazyDRestore.get()).subtract(position()).normalize().scale(0.75));
            if (isControlledByLocalInstance()) {
                move(MoverType.SELF, getDeltaMovement());
            }
        }
        else if (isFlying() && isControlledByLocalInstance()) {
            Vec3 prevMovement = getDeltaMovement().subtract(0, getDeltaMovement().y, 0);
            if (level.isClientSide()) {
                if (isVehicle()) {
                    updateRotationDelta();
                    yRot += yRotDelta;
                    for (Entity passenger : getPassengers()) {
                        // FIXME also turn passengers other than the controlling player
                        passenger.yRot += yRotDelta;
                        if (passenger instanceof LivingEntity) {
                            ((LivingEntity) passenger).yBodyRot += yRotDelta;
                        }
                    }
                    prevMovement = Vec3.directionFromRotation(0, yRot).scale(prevMovement.length());
                }
            }
            double gravity = isNoGravity() ? 0.0D : GRAVITY * (1 + getPassengers().size());
            Vec3 movement = prevMovement.normalize().scale(Math.min(prevMovement.length() + 0.01D, 0.5D));
            setDeltaMovement(movement.x, Math.max(getDeltaMovement().y, 0) + gravity, movement.z);
            move(MoverType.SELF, getDeltaMovement());
        }
    }

    private void updateRotationDelta() {
        float d = 3.5F - (float) getPassengers().size() * 0.5F;
        if (!(inputLeft || inputRight)) {
            if (yRotDelta > 0) {
                yRotDelta = Math.max(yRotDelta - d * 0.05F, 0);
            }
            else if (yRotDelta < 0) {
                yRotDelta = Math.min(yRotDelta + d * 0.05F, 0);
            }
        }
        else {
            yRotDelta = 0;
            if (inputLeft) {
                yRotDelta -= d;
            }
            if (inputRight) {
                yRotDelta += d;
            }
        }
    }
    
    public void setInput(boolean left, boolean right) {
        this.inputLeft = left;
        this.inputRight = right;
    }
    
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || this.is(player.getVehicle())) {
            return InteractionResult.PASS;
        } 
        if (!level.isClientSide()) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        } 
        return InteractionResult.SUCCESS;
    }
    
    
    
    @Override
    protected boolean canAddPassenger(Entity entity) {
        return getPassengers().size() < MAX_PASSENGERS;
    }

    @Override
    public Entity getControllingPassenger() {
        return isVehicle() ? getPassengers().get(0) : null;
    }

    @Override
    protected void addPassenger(Entity entity) {
        if (!isVehicle()) {
            xRot = entity.xRot;
            yRot = entity.yRot;
            entity.setYBodyRot(entity.yRot);
            
            float minGap = 1;
            double groundGap = -CollisionUtil.collide(this, new Vec3(0, -minGap, 0)).y;
            if (groundGap < minGap) {
                float liftUp = minGap - (float) groundGap;
                move(MoverType.SELF, new Vec3(0, entity.getBbHeight() + liftUp, 0));
            }

            Vec3 riderMovement = entity.getDeltaMovement().multiply(1, 0, 1);
            Vec3 gliderRotVec = Vec3.directionFromRotation(0, yRot);
            // TODO add the entity's movement (when making a glider after leap)
            setDeltaMovement(gliderRotVec.scale(Math.max(riderMovement.dot(gliderRotVec), 0.05D)));
        }
        super.addPassenger(entity);
        if (isControlledByLocalInstance() && lerpSteps > 0) {
            lerpSteps = 0;
            absMoveTo(lerpX, lerpY, lerpZ, (float) lerpYRot, (float) lerpXRot);
        }
        updateBbHeight();
        if (!level.isClientSide() && entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                if (power.getType() == ModPowers.HAMON.get()) {
                    passengerPowers.add(power);
                }
            });
        }
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        updateBbHeight();
        if (!level.isClientSide() && entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> passengerPowers.remove(power));
        }
    }
    
    private void updateBbHeight() {
        passengersHeight = isVehicle() ? getPassengers().stream().max(Comparator.comparingDouble(Entity::getBbHeight)).get().getBbHeight() : 0;
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions defaultSize = super.getDimensions(pose);
        return new EntityDimensions(defaultSize.width, defaultSize.height + passengersHeight, defaultSize.fixed);
    }
    
    
    
    private static final Vec3[] OFFSETS = {
        new Vec3(0, 0, 0.625), 
        new Vec3(0.625, 0, 0), 
        new Vec3(-0.625, 0, 0), 
        new Vec3(0, 0, -0.625)
    };
    @Override
    public void positionRider(Entity entity) {
        if (hasPassenger(entity)) {
            int i = getPassengers().indexOf(entity);
            if (i < MAX_PASSENGERS) {
                Vec3 rotatedVec = OFFSETS[i].yRot(-yRot * MathUtil.DEG_TO_RAD);
                entity.setPos(
                        getX() + rotatedVec.x, 
                        getY(1.0) - super.getDimensions(Pose.STANDING).height - entity.getBbHeight(), 
                        getZ() + rotatedVec.z);
            }
        }
    }
    
    

    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        }
        Entity source = dmgSource.getDirectEntity();
        if (source != null && this.is(source.getVehicle())) {
            return false;
        }
        if (!level.isClientSide && isAlive()) {
            if (source instanceof LivingEntity) {
                float energy = Math.min(getEnergy(), 100);
                DamageUtil.dealHamonDamage((LivingEntity) source, energy * 0.04F, this, null);
                setEnergy(getEnergy() - energy);
            }
            setHealth(getHealth() - amount);
            markHurt();
            return true;
        } else {
            return true;
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource dmgSource) {
       return super.isInvulnerableTo(dmgSource) || "hamon".startsWith(dmgSource.getMsgId());
    }
    
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    public void push(Entity entity) {}

    @Override
    public boolean isPickable() {
        return !level.isClientSide() || ClientUtil.getClientPlayer().getRootVehicle() != this.getRootVehicle();
    }

    private void setIsFlying(boolean isGliding) {
        entityData.set(IS_FLYING, isGliding);
    }

    public boolean isFlying() {
        return entityData.get(IS_FLYING);
    }
    
    public float getYRotDelta() {
        return yRotDelta;
    }

    public void setEnergy(float energy) {
        entityData.set(ENERGY, energy);
    }
    
    public float getEnergy() {
        return entityData.get(ENERGY);
    }
    
    @Override
    public float getHealth() {
        return entityData.get(HEALTH);
    }

    public void setHealth(float health) {
        entityData.set(HEALTH, Mth.clamp(health, 0, getMaxHealth()));
    }

    @Override
    public float getMaxHealth() {
        return MAX_HEALTH;
    }
    
    public void setLeavesBlock(BlockState block) {
        if (block != null && block.getBlock() != Blocks.AIR) {
            this.leavesBlock = block;
            this.leavesBlockTex = null;
        }
    }
    
    public BlockState getLeavesBlock() {
        return leavesBlock;
    }
    
    @Nullable
    public ResourceLocation getLeavesTexture() {
        return leavesBlockTex;
    }
    
    public void setLeavesTex(ResourceLocation texture) {
        this.leavesBlockTex = texture;
    }
    
    public void setFoliageColor(int color) {
        this.foliageColor = color;
    }
    
    public int getFoliageColor() {
        return foliageColor;
    }
    
    private float prevHealth = 0;
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> parameter) {
        super.onSyncedDataUpdated(parameter);
        if (level.isClientSide()) {
            if (IS_FLYING.equals(parameter) && isFlying()) {
                ClientTickingSoundsHelper.playGliderFlightSound(this);
            }
            else if (HEALTH.equals(parameter)) {
                float health = entityData.get(HEALTH);
                if (health < prevHealth) {
                    float diff = prevHealth - health;
                    addLeavesParticles(Math.max((int) (diff * 100), 1));
                    
                    SoundType soundType = leavesBlock.getSoundType();
                    Vec3 soundPos = clSoundPos();
                    level.playLocalSound(soundPos.x, soundPos.y, soundPos.z, 
                            soundType.getHitSound(), getSoundSource(), 
                            (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.8F, false);
                }
                prevHealth = health;
            }
        }
    }
    
    private Vec3 clSoundPos() {
        Player clientPlayer = ClientUtil.getClientPlayer();
        return clientPlayer.getVehicle() == this ? 
                new Vec3(clientPlayer.getX(), this.getY(1.0F), clientPlayer.getZ()) 
                : new Vec3(this.getX(), this.getY(1.0F), this.getZ());
    }
    
    private void addLeavesParticles(int count) {
        ParticleOptions leavesParticle = new BlockParticleOption(ParticleTypes.BLOCK, leavesBlock);
        for (int i = 0; i < count; i++) {
            level.addParticle(leavesParticle, 
                    getRandomX(0.5F), 
                    getY(1.0F), 
                    getRandomZ(0.5F), 0, 0, 0);
        }
    }
    
    private int resetCrazyDTimer;
    @Override
    public boolean crazyDRestore(BlockPos blockPos) {
        if (position().distanceToSqr(Vec3.atCenterOf(blockPos)) < 0.25) {
            discard();
            return true;
        }
        else {
            entityData.set(CRAZY_D_RESTORE, Optional.of(blockPos));
            resetCrazyDTimer = 2;
            return false;
        }
    }
    
    private void tickCrazyDFlag() {
        if (resetCrazyDTimer > 0 && --resetCrazyDTimer == 0) {
            entityData.set(CRAZY_D_RESTORE, Optional.empty());
        }
    }
    
    @Override
    protected void defineSynchedData() {
        entityData.define(IS_FLYING, false);
        entityData.define(ENERGY, MAX_ENERGY);
        entityData.define(HEALTH, MAX_HEALTH);
        entityData.define(HAMON_USERS_CHARGING, (byte) 0);
        entityData.define(CRAZY_D_RESTORE, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        setIsFlying(nbt.getBoolean("Flight"));
        if (nbt.contains("Energy")) setEnergy(nbt.getFloat("Energy"));
        if (nbt.contains("Health")) setHealth(nbt.getFloat("Health"));
        if (nbt.contains("Color"))  foliageColor = nbt.getInt("Color");
        if (nbt.contains("Block", MCUtil.getNbtId(CompoundTag.class))) {
            setLeavesBlock(NbtUtils.readBlockState(nbt.getCompound("Block")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putBoolean("Flight", isFlying());
        nbt.putFloat("Energy", getEnergy());
        nbt.putFloat("Health", getHealth());
        if (foliageColor >= 0) {
            nbt.putInt("Color", foliageColor);
        }
        nbt.put("Block", NbtUtils.writeBlockState(leavesBlock));
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(Block.getId(leavesBlock));
        buffer.writeInt(foliageColor);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        setLeavesBlock(GameData.getBlockStateIDMap().byId(additionalData.readVarInt()));
        
        foliageColor = additionalData.readInt();
        if (foliageColor < 0) {
            foliageColor = ClientUtil.getFoliageColor(leavesBlock, level, this.blockPosition());
            PacketManager.sendToServer(new ClLeavesGliderColorPacket(getId(), foliageColor));
        }
    }

}
