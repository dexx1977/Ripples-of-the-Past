package com.github.standobyte.jojo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.TimerQueue;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.nbt.Tag;

public class AngeloRockEntity extends Entity implements IEntityAdditionalSpawnData {
    protected static final EntityDataAccessor<Optional<BlockPos>> DATA_ATTACH_POS_ID = SynchedEntityData.defineId(AngeloRockEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    protected static final EntityDataAccessor<Boolean> CREATION_COMPLETE = SynchedEntityData.defineId(AngeloRockEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(AngeloRockEntity.class, EntityDataSerializers.FLOAT);
    private static final int CREATION_ANIM_LEN = 40;
    private int creationAnimTicks;
    private Map<BlockPos, PrevBlockInfo> angeloRockBlocks = new HashMap<>();
    private List<ItemStack> itemDrops = new ArrayList<>();
    private boolean startedSound;
    private EntityOwnerResolver angeloEntity = new EntityOwnerResolver();
    public boolean keepMobInside;
    /* fuck it, sure, yeah */ private Mob mob;
    private boolean useMobHurtSound;
    private TimerQueue responseSoundTimer = new TimerQueue(false);
    
    
    public AngeloRockEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }
    
    public static AngeloRockEntity turnIntoRock(Level world, Entity entity, Vec3 rockPos, float yRot, 
            PrevBlockInfo... angeloRockBlocks) {
        if (world.isClientSide()) {
            return null;
        }
        
        AngeloRockEntity angeloRock = new AngeloRockEntity(ModEntityTypes.ANGELO_ROCK.get(), world);
        angeloRock.yRot = yRot;
        angeloRock.setPos(rockPos.x, rockPos.y, rockPos.z);
        
        angeloRock.creationAnimTicks = CREATION_ANIM_LEN;
        if (entity instanceof LivingEntity) {
            angeloRock.angeloEntity.setThrower(entity);
        }
        
        if (angeloRockBlocks != null) {
            for (PrevBlockInfo block : angeloRockBlocks) {
                if (block != null) {
                    angeloRock.angeloRockBlocks.put(block.pos, block);
                }
            }
        }
        
        world.addFreshEntity(angeloRock);
        return angeloRock;
    }
    
    public void setBlockDrops(List<ItemStack> itemDrops) {
        this.itemDrops = itemDrops;
    }
    
    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        if (!isFullyFormed()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        else {
            if (mob != null) {
                mob.setPos(getX(), getY(), getZ());
            }
            SoundEvent voiceline = IStandPower.getStandPowerOptional(pPlayer).resolve().map(power -> {
                StandType<?> stand = power.getType();
                if (stand == ModStands.CRAZY_DIAMOND.getStandType()) {
                    return ModSounds.JOSUKE_YO_ANGELO.get();
                }
                if (stand != null && stand.getRegistryName().getPath().contains("echoes")) {
                    return ModSounds.KOICHI_YO_ANGELO.get();
                }
                return null;
            }).orElse(null);
            if (voiceline != null && JojoModUtil.sayVoiceLine(pPlayer, voiceline, null, 1, 1, 0, true)) {
                responseSoundTimer.add(30);
            }
            else {
                playMobResponseSound();
            }
            return InteractionResult.CONSUME;
        }
    }
    
    public void playMobResponseSound() {
        if (mob != null) {
            if (useMobHurtSound) {
                CommonReflection.playHurtSound(mob, mob.level().damageSources().generic());
            }
            else {
                mob.playAmbientSound();
            }
        }
    }
    
    private void tickResponseTimers() {
        if (!level.isClientSide()) {
            responseSoundTimer.tick(this::playMobResponseSound);
        }
    }
    
    
    @Override
    public boolean hurt(DamageSource dmgSource, float dmgAmount) {
        if (level.isClientSide()) return false;
        
        if ("player".equals(dmgSource.getMsgId()) && dmgSource.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) dmgSource.getEntity();
            boolean creative = attacker instanceof Player && ((Player) attacker).abilities.instabuild;
            if (creative) {
                dropMode = DropMode.NONE;
                
                lastAttack = dmgSource;
                cancelPlayerHitSound = true;
                
                breakRock();
                return true;
            }
            
            ItemStack item = attacker.getMainHandItem();
            if (!item.isEmpty() && item.getItem() instanceof PickaxeItem) {
                Collection<BlockState> blocks = new ArrayList<>();
                blocks.add(getLowerBlock());
                blocks.add(getUpperBlock());
                dmgAmount = (float) blocks.stream().mapToDouble(item::getDestroySpeed).average().getAsDouble();
                int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, item);
                if (i > 0) {
                    dmgAmount += (float)(i * i + 1);
                }
                if (blocks.stream().noneMatch(block -> !block.requiresCorrectToolForDrops() || item.isCorrectToolForDrops(block))) {
                    dmgAmount *= 0.3f;
                }
                
                dmgAmount = Math.max(dmgAmount, 1);
                BlockState randomBlock = blocks.stream().skip(random.nextInt(blocks.size())).findFirst().get();
                SoundType blockSound = randomBlock.getSoundType();
                level.playSound(null, getX(), getY(0.5), getZ(), blockSound.getHitSound(), 
                        getSoundSource(), (blockSound.getVolume() + 1.0F) / 8.0F, blockSound.getPitch() * 0.5F);
                
                // TODO angelo rock silk touch
//                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, item) > 0) {
//                    dropMode = DropMode.SILK_TOUCH;
//                }
                
                lastAttack = dmgSource;
                cancelPlayerHitSound = true;
                
                entityData.set(DAMAGE, entityData.get(DAMAGE) + dmgAmount);
                if (!creative && isBroken()) {
                    item.hurt(1, random, attacker instanceof ServerPlayer ? (ServerPlayer) attacker : null);
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    public void breakRock() {
        if (!level.isClientSide()) {
            entityData.set(DAMAGE, Float.MAX_VALUE);
        }
    }
    
    private void onDamageApplied() {
        if (isBroken()) {
            doBreakRock();
        }
    }
    
    private boolean isBroken() {
        return entityData.get(DAMAGE) >= 40;
    }
    
    private DamageSource lastAttack;
    private DropMode dropMode = DropMode.BLOCKS;
    
    private enum DropMode {
        BLOCKS,
        NONE,
        SILK_TOUCH
    }
    
    private void doBreakRock() {
        if (!level.isClientSide()) {
            if (!level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                dropMode = DropMode.NONE;
            }
            angeloRockBlocks.values().forEach(block -> {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(level, block.pos, block.state, Optional.empty(), block.drops);
            });
            Vec3 pos = position();
            // TODO angelo rock silk touch
            if (dropMode == DropMode.SILK_TOUCH) {
                
            }
            else {
                if (dropMode == DropMode.BLOCKS) {
                    for (ItemStack item : itemDrops) {
                        ItemEntity itemEntity = new ItemEntity(level, pos.x + 0.5, pos.y, pos.z + 0.5, item);
                        itemEntity.setDefaultPickUpDelay();
                        if (captureDrops() != null) {
                            captureDrops().add(itemEntity);
                        }
                        else {
                            level.addFreshEntity(itemEntity);
                        }
                    }
                }
                
                if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) && mob != null && mob.isRemoved() && lastAttack != null) {
                    mob.setPos(getX(), getY(), getZ());
                    mobLootFortune = mob;
                    mob.lastHurtByPlayerTime = 1;
                    CommonReflection.dropAllDeathLoot(mob, lastAttack);
                    mobLootFortune = null;
                }
            }
            
            remove();
        }
        else {
            clBreakBlockVisuals(getUpperBlock(), blockPosition());
            clBreakBlockVisuals(getLowerBlock(), blockPosition().above());
        }
    }
    
    private void clBreakBlockVisuals(BlockState blockState, BlockPos blockPos) {
        CustomParticlesHelper.addBlockBreakParticles(blockPos, blockState);
        SoundType soundType = blockState.getSoundType();
        SoundEvent sound = soundType.getBreakSound();
        level.playLocalSound(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                sound, getSoundSource(), (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
    }
    
    public float getDamageRatio() {
        return entityData.get(DAMAGE) / (20 * angeloRockBlocks.size());
    }
    
    
    public static boolean cancelPlayerHitSound = false;
    public static Mob mobLootFortune;
    
    
    @Override
    public boolean canBeCollidedWith() {
        return isAlive();
    }
    
    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
        this.entityData.define(CREATION_COMPLETE, false);
        this.entityData.define(DAMAGE, 0f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        BlockPos blockpos = this.getAttachPosition();
        if (blockpos != null) {
            pCompound.putInt("APX", blockpos.getX());
            pCompound.putInt("APY", blockpos.getY());
            pCompound.putInt("APZ", blockpos.getZ());
        }
        pCompound.putBoolean("Created", entityData.get(CREATION_COMPLETE));
        pCompound.putInt("CreationAnim", creationAnimTicks);
        pCompound.putFloat("RockDamage", entityData.get(DAMAGE));
        
        if (!angeloRockBlocks.isEmpty()) {
            ListTag blocksNbt = new ListTag();
            for (PrevBlockInfo block : angeloRockBlocks.values()) {
                blocksNbt.add(block.toNBT());
            }
            pCompound.put("RockBlocks", blocksNbt);
        }
        
        if (!itemDrops.isEmpty()) {
            ListTag blockDropsNbt = new ListTag();
            for (ItemStack item : itemDrops) {
                if (!item.isEmpty()) {
                    blockDropsNbt.add(item.save(new CompoundTag()));
                }
            }
            pCompound.put("BlockDrops", blockDropsNbt);
        }
        
        pCompound.putBoolean("KeepMob", keepMobInside);
        if (mob != null) {
            String s = mob.getEncodeId();
            if (s != null) {
                CompoundTag mobNBT = new CompoundTag();
                mobNBT.putString("id", s);
                mob.saveWithoutId(mobNBT);
                mobNBT.remove("Passengers");
                pCompound.put("AngeloMob", mobNBT);
                pCompound.putBoolean("NoAmbient", useMobHurtSound);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.contains("APX")) {
            int i = pCompound.getInt("APX");
            int j = pCompound.getInt("APY");
            int k = pCompound.getInt("APZ");
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(i, j, k)));
        } else {
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
        }
        entityData.set(CREATION_COMPLETE, pCompound.getBoolean("Created"));
        this.creationAnimTicks = pCompound.getInt("CreationAnim");
        entityData.set(DAMAGE, pCompound.getFloat("RockDamage"));
        
        MCUtil.getNbtElement(pCompound, "RockBlocks", ListTag.class).ifPresent(blocksNbt -> {
            if (blocksNbt.getElementType() != Tag.TAG_COMPOUND) return;
            blocksNbt.forEach(blockNbt -> {
                PrevBlockInfo block = PrevBlockInfo.fromNBT((CompoundTag) blockNbt);
                if (block != null) {
                    angeloRockBlocks.put(block.pos, block);
                }
            });
        });
        
        itemDrops.clear();
        pCompound.getList("BlockDrops", Tag.TAG_COMPOUND).forEach(itemNbt -> {
            ItemStack item = ItemStack.of((CompoundTag) itemNbt);
            if (!item.isEmpty()) {
                itemDrops.add(item);
            }
        });
        
        keepMobInside = pCompound.getBoolean("KeepMob");
        Entity mobEntity = MCUtil.nbtGetCompoundOptional(pCompound, "AngeloMob").flatMap(mobNBT -> {
            try {
                return EntityType.create(mobNBT, level);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }).orElse(null);
        this.mob = mobEntity instanceof Mob ? (Mob) mobEntity : null;
        if (mob != null) {
            mob.discard();
        }
        this.useMobHurtSound = pCompound.getBoolean("NoAmbient");
    }
    
    @Override
    public void tick() {
        super.tick();
        cancelPlayerHitSound = false;
        
        if (!level.isClientSide()) {
            tickResponseTimers();
        }
        
        if (creationAnimTicks > 0) {
            if (level.isClientSide()) {
                if (ClientUtil.canSeeStands()) {
                    CrazyDiamondHeal.addParticlesAround(this);
                }
                if (ClientUtil.canHearStands() && !this.isSilent()) {
                    if (!startedSound) {
                        level.playSound(ClientUtil.getClientPlayer(), getX(), getY(), getZ(), 
                                ModSounds.CRAZY_DIAMOND_FIX_STARTED.get(), getSoundSource(), 1, 1);
                        ClientTickingSoundsHelper.playStoppableEntitySound(this, 
                                ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), 1, 1, true, entity -> entity.creationAnimTicks > 0);
                        startedSound = true;
                    }
                    if (creationAnimTicks == 1) {
                        level.playSound(ClientUtil.getClientPlayer(), getX(), getY(), getZ(), 
                                ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), getSoundSource(), 1, 1);
                    }
                }
            }
            --creationAnimTicks;
        }
        
        LivingEntity angeloEntity = this.angeloEntity.getEntityLiving(level);
        if (angeloEntity != null && !entityData.get(CREATION_COMPLETE)) {
            angeloEntity.hurtTime = 0;
            angeloEntity.deathTime = 0;
            angeloEntity.walkAnimation.position = 0;
            angeloEntity.walkAnimation.speed = 0;
            angeloEntity.walkAnimation.speedOld = 0;
            angeloEntity.addEffect(new MobEffectInstance(ModStatusEffects.IMMOBILIZE.get(), 10, 0, false, false, true));
            angeloEntity.moveTo(getX(), getY(), getZ());
            angeloEntity.setPose(Pose.STANDING);
            if (creationAnimTicks <= 0) {
                if (!level.isClientSide()) {
                    if (angeloEntity instanceof IPlayerPossess) {
                        angeloEntity.removeAllEffects();
                        ((IPlayerPossess) angeloEntity).jojoPossessEntity(this, false, null);
                    }
                    else {
                        angeloEntity.discard();
                        if (keepMobInside && angeloEntity instanceof Mob) {
                            this.mob = (Mob) angeloEntity;
                            useMobHurtSound = CommonReflection.getAmbientSound(mob) == null;
                        }
                    }
                }
                entityData.set(CREATION_COMPLETE, true);
                this.angeloEntity.setThrower(null);
            }
        }
        
        BlockPos attachPos = getAttachPosition();
        if (attachPos == null && !this.level.isClientSide) {
            attachPos = this.blockPosition();
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(attachPos));
        }

        if (this.isPassenger()) {
            attachPos = null;
            float f = this.getVehicle().yRot;
            this.yRot = f;
        } else if (!this.level.isClientSide) {
            Optional<BlockPos> moveWithPiston = moveWithPiston(attachPos);
            if (!moveWithPiston.isPresent()) moveWithPiston = moveWithPiston(attachPos.above());
            if (moveWithPiston.isPresent()) {
                this.entityData.set(DATA_ATTACH_POS_ID, moveWithPiston);
            }
        }

        if (attachPos != null) {
            setPosAndOldPos(attachPos.getX() + 0.5, attachPos.getY(), attachPos.getZ() + 0.5);
//            if (isAddedToWorld() && level instanceof ServerWorld) {
//                ((ServerWorld)level).updateChunkPos(this); // Forge - Process chunk registration after moving.
//            }
            setBoundingBox(new AABB(
                    getX() - 0.5, 
                    getY(), 
                    getZ() - 0.5, 
                    getX() + 0.5, 
                    getY() + getBbHeight(), 
                    getZ() + 0.5));
        }
    }
    
    @Nullable
    private Optional<BlockPos> moveWithPiston(BlockPos pistonBlockPos) {
        Direction pistonHeadDir = null;
        BlockState blockState = level.getBlockState(pistonBlockPos);
        if (!blockState.isAir() && (blockState.is(Blocks.MOVING_PISTON) || blockState.is(Blocks.PISTON_HEAD))) {
            pistonHeadDir = blockState.getValue(BlockStateProperties.FACING);
        }
        if (pistonHeadDir != null) {
            BlockPos attachPos = getAttachPosition();
            if (attachPos != null) {
                BlockPos newPos = attachPos.relative(pistonHeadDir);
                if (this.level.isEmptyBlock(newPos) && this.level.isEmptyBlock(newPos.above())) {
                    return Optional.of(newPos);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public void setPos(double pX, double pY, double pZ) {
        super.setPos(pX, pY, pZ);
        if (this.entityData != null && this.tickCount != 0) {
            Optional<BlockPos> optional = this.entityData.get(DATA_ATTACH_POS_ID);
//            if (this.isAddedToWorld() && this.level instanceof ServerWorld) {
//                ((ServerWorld) level).updateChunkPos(this); // Forge - Process chunk registration after moving.
//            }
            Optional<BlockPos> optional1 = Optional.of(new BlockPos(pX, pY, pZ));
            if (!optional1.equals(optional)) {
                this.entityData.set(DATA_ATTACH_POS_ID, optional1);
                this.hasImpulse = true;
            }

        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_ATTACH_POS_ID.equals(pKey)) {
            if (level.isClientSide && !isPassenger()) {
                BlockPos blockpos = getAttachPosition();
                if (blockpos != null) {
                    setPosAndOldPos(blockpos.getX() + 0.5, blockpos.getY(), blockpos.getZ() + 0.5);
                }
            }
        }
        else if (DAMAGE.equals(pKey)) {
            onDamageApplied();
        }

        super.onSyncedDataUpdated(pKey);
    }

    @Nullable
    public BlockPos getAttachPosition() {
        return this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
    }

    public void setAttachPosition(@Nullable BlockPos pPos) {
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(pPos));
    }
    
    public float getCreationAnimProgress(float partialTick) {
        return creationAnimTicks <= 0 ? 1 : (CREATION_ANIM_LEN - creationAnimTicks + partialTick) / CREATION_ANIM_LEN;
    }
    
    public boolean isFullyFormed() {
        return creationAnimTicks <= 0;
    }
    
    
    public BlockState getUpperBlock() {
        return getBlock(blockPosition().above());
    }
    
    public BlockState getLowerBlock() {
        return getBlock(blockPosition());
    }
    
    public BlockState getBlock(BlockPos pos) {
        PrevBlockInfo block = angeloRockBlocks.get(pos);
        return block != null ? block.state : Blocks.STONE.defaultBlockState();
    }
    
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        NetworkUtil.writeOptionally(buffer, angeloEntity.getEntityLiving(level), entity -> buffer.writeInt(entity.getId()));
        buffer.writeVarInt(creationAnimTicks);
        NetworkUtil.writeCollection(buffer, angeloRockBlocks.values(), PrevBlockInfo::toBuf, false);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity angeloEntity = NetworkUtil.readOptional(additionalData, buf -> ClientUtil.getEntityById(buf.readInt())).orElse(null);
        if (angeloEntity instanceof LivingEntity) {
            this.angeloEntity.setThrower(angeloEntity);
        }
        creationAnimTicks = additionalData.readVarInt();
        angeloRockBlocks.clear();
        NetworkUtil.readCollection(additionalData, PrevBlockInfo::fromBuf).forEach(block -> angeloRockBlocks.put(block.pos, block));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
