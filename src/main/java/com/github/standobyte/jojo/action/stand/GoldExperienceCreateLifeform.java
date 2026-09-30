package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.config.ActionConfigField;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.capability.entity.LifeformsMetMobs;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.GETransformationEntity.GETransformationData;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.MolotovEntity;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.MolotovItem;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.mrpresident.MrPresidentStandType;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.LazyOptional;

public class GoldExperienceCreateLifeform extends StandAction {
    @ActionConfigField public double maxLifeformDistance = 128;

    public GoldExperienceCreateLifeform(StandAction.Builder builder) {
        super(builder);
        voiceLineDelay = Integer.MAX_VALUE;
    }
    
    @Override
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, Level world, LivingEntity user, IStandPower power) {
        Entity aimingEntity = StandUtil.getStandIfInManualControl(power);
        Vec3 startPos = aimingEntity.getEyePosition(1.0F);
        double distance = Math.sqrt(getMaxRangeSqBlockTarget());
        Vec3 rtVec = aimingEntity.getViewVector(1.0F).scale(distance);
        Vec3 endPos = startPos.add(rtVec);
        AABB aabb = aimingEntity.getBoundingBox().expandTowards(rtVec).inflate(1);
        HitResult rayTrace = JojoModUtil.rayTraceMultipleEntities(startPos, endPos, aabb, 
                distance, world, aimingEntity, 
                e -> e instanceof ItemEntity, false, ClipContext.Block.COLLIDER, 
                0, 0)[0];
        if (rayTrace.getType() == HitResult.Type.ENTITY) {
            targetContainer.set(ActionTarget.fromRayTraceResult(rayTrace));
        }
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            Entity entity = target.getEntity();
            if (entity instanceof ItemEntity) {
                ItemStack item = ((ItemEntity) entity).getItem();
                return HamonUtil.isItemLivingMatter(item) ? conditionMessage("ge_lifeform_material_item") : ActionConditionResult.POSITIVE;
            }
            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! use more types of inanimate entities as targets?
            return ActionConditionResult.noMessage(
                    entity instanceof PrimedTnt || 
                    entity instanceof RoadRollerEntity || 
                    entity instanceof EndCrystal || 
                    entity instanceof Boat);
        case BLOCK:
            if (!JojoModUtil.breakingBlocksEnabled(user.level)) {
                return ActionConditionResult.NEGATIVE;
            }
            if (!power.isUserCreative()) {
                Level world = user.level;
                BlockPos blockPos = target.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);
                
                float blockHardness = blockState.getDestroySpeed(world, blockPos);
                if (blockHardness < 0) {
                    return ActionConditionResult.NEGATIVE;
                }
            }
        default:
            break;
        }
        
        return super.checkTarget(target, user, power);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.level.isClientSide() && getChosenEntityType(ClientUtil.getClientPlayer()) == null) {
            return ActionConditionResult.NEGATIVE;
        }
        
        int mobsCreated = (int) StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), -1).count();
        if (mobsCreated >= 16) {
            return conditionMessage("ge_too_many_mobs");
        }

        if (target.getType() == TargetType.ENTITY
                || GoldExperienceMarkItem.getTargetedMarkedItem(power, user).isPresent()) {
            return ActionConditionResult.POSITIVE;
        }
        
        boolean hasAnItem = false;
        boolean itemFits = false;
        boolean hasABlock = false;
        boolean blockFits = false;
        boolean canUseBlock = JojoModUtil.breakingBlocksEnabled(user.level);
        
        ItemStack item = user.getItemInHand(InteractionHand.OFF_HAND);
        if (!item.isEmpty()) {
            hasAnItem = true;
            itemFits = canGiveLifeTo(item);
        }
        
        if (canUseBlock && target.getType() == TargetType.BLOCK) {
            hasABlock = true;
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = user.level.getBlockState(blockPos);
            blockFits = !HamonOrganismInfusion.isBlockLiving(blockState);
        }
        
        if (!hasAnItem && !hasABlock) {
            return canUseBlock ? conditionMessage("ge_lifeform_material") : conditionMessage("ge_lifeform_material_only_item");
        }
        if (!itemFits && !blockFits) {
            if (hasAnItem) {
                return conditionMessage("ge_lifeform_material_item");
            }
            else {
                return conditionMessage("ge_lifeform_material_block");
            }
        }
        
        return ActionConditionResult.POSITIVE;
    }
    
    public static boolean canGiveLifeTo(ItemStack item) {
        return !HamonUtil.isItemLivingMatter(item) || item.getItem() instanceof MobBucketItem;
    }
    
    @Override
    public void clWriteExtraData(FriendlyByteBuf buf) {
        Player player = ClientUtil.getClientPlayer();
        NetworkUtil.writeOptionally(buf, 
                getChosenEntityType(player), 
                EntitySubtype::toBuf);
        
        Optional<UUID> trackedItemUUID = GoldExperienceMarkItem
                .getTargetedMarkedItem(IStandPower.getPlayerStandPower(player), player)
                .map(TrackerItemStack::getTrackerId);
        NetworkUtil.writeOptional(buf, trackedItemUUID, buf::writeUUID);
    }
    
    @Nullable
    public static EntitySubtype<?> getChosenEntityType(Player player) {
//        ItemStack heldItem = player.getItemInHand(Hand.OFF_HAND);
//        if (!heldItem.isEmpty()) {
//            Item item = heldItem.getItem();
//            if (item == Items.SLIME_BALL || item == Items.SLIME_BLOCK) {
//                return EntityType.SLIME;
//            }
//            if (item == Items.MAGMA_CREAM || item == Items.MAGMA_BLOCK) {
//                return EntityType.MAGMA_CUBE;
//            }
//        }
//
//        if (target.getType() == TargetType.BLOCK) {
//            BlockPos blockPos = target.getBlockPos();
//            BlockState blockState = world.getBlockState(blockPos);
//            Block block = blockState.getBlock();
//            if (block == Blocks.SLIME_BLOCK) {
//                return EntityType.SLIME;
//            }
//            if (block == Blocks.MAGMA_BLOCK) {
//                return EntityType.MAGMA_CUBE;
//            }
//        }
        
        return player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve()
                .map(playerData -> playerData.getGELifeformsUIState().getGEChosenLifeformType()).orElse(null);
    }
    
    public static Entity createEntity(EntitySubtype<?> type, Level world, LivingEntity standUser) {
        Entity lifeFormCreated = type.create(world);
        CompoundTag nbt = new CompoundTag();
        nbt.putString("DeathLootTable", "empty");
        if (!world.dimensionType().piglinSafe()) {
            nbt.putBoolean("IsImmuneToZombification", true);
        }
        lifeFormCreated.load(nbt);
        
        if (lifeFormCreated instanceof Mob) {
            ((Mob) lifeFormCreated).finalizeSpawn((ServerLevel) world, 
                    world.getCurrentDifficultyAt(standUser.blockPosition()), 
                    MobSpawnType.COMMAND, null, null);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                lifeFormCreated.setItemSlot(slot, ItemStack.EMPTY);
            }
            
            if (lifeFormCreated instanceof AgeableMob) {
                standUser.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(playerData -> {
                    playerData.animalAgeCd += 3000;
                    ((AgeableMob) lifeFormCreated).setAge(Math.max(playerData.animalAgeCd - 3000, 0));
                });
                if (lifeFormCreated instanceof AbstractHorse) {
                    ((AbstractHorse) lifeFormCreated).setTemper(0);
                }
            }
            else if (lifeFormCreated instanceof Slime) {
                CompoundTag additionalNbt = lifeFormCreated.serializeNBT();
                additionalNbt.putInt("Size", 0);
                lifeFormCreated.load(additionalNbt);
            }
        }
        
        return lifeFormCreated;
    }
    
    @Override
    public void perform(Level world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable FriendlyByteBuf extraInput) {
        if (!world.isClientSide() && extraInput != null) {
            EntitySubtype<?> type = NetworkUtil.readOptional(extraInput, EntitySubtype::fromBuf).orElse(null);
            UUID itemTrackerId = NetworkUtil.readOptional(extraInput, extraInput::readUUID).orElse(null);
            if (type != null
                    && GeneralUtil.orElseFalse(user.getCapability(PlayerUtilCapProvider.CAPABILITY), 
                            cap -> cap.metEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, world)) {
                
                Entity lifeFormCreated = createEntity(type, world, user);
                int ticks = getTicksToCreate(user, power, lifeFormCreated);
                
                Entity performer = getControlledEntity(user, power);
                GETransformationEntity tf = new GETransformationEntity(world);
                
                ObjectWrapper<Component> customName = new ObjectWrapper<>(null);
                boolean tfTargetFound = false;
                
                ObjectWrapper<Entity> nonUserItemHolder = new ObjectWrapper<>(null);
                
                // marked item...
                if (itemTrackerId != null) {
                    TrackerItemStack itemTracker = SidedItemTrackerMap.getSidedTrackers(world).getTracker(itemTrackerId);
                    if (itemTracker != null && itemTracker.checkItemIsThere((ServerLevel) world)) {
                        // ...from entity
                        Entity itemEntity = itemTracker.getAtEntity(world);
                        LivingEntity livingItemHolder = itemEntity instanceof LivingEntity ? (LivingEntity) itemEntity : null;
                        if (itemEntity != null) {
                            KnownItemState itemState = itemTracker.getItemState();
                            if (itemState != null) {
                                tfTargetFound = true;
                                Vec3 pos = itemEntity.position();
                                
                                switch (itemState) {
                                case ENTITY_HAS_ITEM:
                                    tf.moveTo(pos.x, pos.y, pos.z, itemEntity.yRot, 0);
                                    mobFromInventory(tf, itemTracker, world, 
                                            livingItemHolder != null ? livingItemHolder : user, 
                                            itemEntity.blockPosition(), customName);
                                    if (itemEntity != user) {
                                        nonUserItemHolder.set(itemEntity);
                                    }
                                    
                                    break;
                                case ENTITY_IS_ITEM:
                                    mobFromEntity(tf, itemEntity, user);
                                    break;
                                case STUCK_ARROW:
                                    tf.moveTo(pos.x, pos.y, pos.z, itemEntity.yRot, itemEntity.xRot);
                                    AbstractArrow arrow = new Arrow(world, user);
                                    arrow.pickup = AbstractArrow.PickupStatus.ALLOWED;
                                    tf.getTfSourceData().withEntitySource(arrow);
                                    if (livingItemHolder != null) {
                                        decrementStuckArrow(livingItemHolder);
                                        tf.withHost(livingItemHolder);
                                        tf.getTfSourceData().withFollowTarget(livingItemHolder.getUUID(), GETransformationEntity.FollowTargetMode.AGGRO_TRACK, user);
                                    }
                                    break;
                                case STUCK_KNIFE:
                                    tf.moveTo(pos.x, pos.y, pos.z, itemEntity.yRot, itemEntity.xRot);
                                    AbstractArrow knife = new KnifeEntity(world, user);
                                    knife.pickup = AbstractArrow.PickupStatus.ALLOWED;
                                    tf.getTfSourceData().withEntitySource(knife);
                                    if (livingItemHolder != null) {
                                        decrementStuckKnife(livingItemHolder);
                                        tf.withHost(livingItemHolder);
                                        tf.getTfSourceData().withFollowTarget(livingItemHolder.getUUID(), GETransformationEntity.FollowTargetMode.AGGRO_TRACK, user);
                                    }
                                    break;
                                default:
                                    JojoMod.getLogger().error("Didn't handle the case of {} item being inside an entity", itemState);
                                    break;
                                }
                            }
                            else {
                                JojoMod.getLogger().error("Failed to extract tracked item from {} entity", itemEntity.getType().getRegistryName());
                            }
                        }
                        else {
                            // ...or from block
                            BlockPos itemPos = itemTracker.getAtBlockPos();
                            if (itemPos != null) {
                                BlockState blockState = world.getBlockState(itemPos);
                                KnownItemState itemState = itemTracker.getItemState();
                                if (itemState != null) {
                                    tfTargetFound = true;
                                    
                                    switch (itemState) {
                                    case BLOCK_HAS_ITEM:
                                        tf.moveTo(itemPos.getX(), itemPos.getY() + 1, itemPos.getZ(), 0, 0);
                                        mobFromInventory(tf, itemTracker, world, 
                                                user, itemPos.above(), customName);
                                        break;
                                    case BLOCK_IS_ITEM:
                                        tfTargetFound = false;
                                        break;
                                    default:
                                        JojoMod.getLogger().error("Didn't handle the case of {} item being inside a block", itemState);
                                        break;
                                    }
                                }
                                else {
                                    JojoMod.getLogger().error("Failed to extract tracked item from {} block at {}", blockState.getBlock().getRegistryName(), itemPos);
                                }
                            }
                        }
                    }
                }
                
                // targeted non-living entity
                if (!tfTargetFound && target.getType() == TargetType.ENTITY) {
                    Entity targetEntity = target.getEntity();
                    tfTargetFound = true;
                    mobFromEntity(tf, targetEntity, user);
                }
                
                // item held in off-hand
                if (!tfTargetFound) {
                    ItemStack heldItem = user.getItemInHand(InteractionHand.OFF_HAND);
                    if (!heldItem.isEmpty() && canGiveLifeTo(heldItem)) {
                        tfTargetFound = true;
                        
                        Vec3 pos = performer.position();
                        Vec3 lookVec = performer.getLookAngle();
                        double distScale = lifeFormCreated.getBbWidth() + 1;
                        pos = pos.add(lookVec.x * distScale, 0, lookVec.z * distScale);
                        tf.moveTo(pos.x, pos.y, pos.z, performer.yRot, 0);
                        
                        mobFromInventory(tf, heldItem, world, 
                                user, performer.blockPosition(), customName);
                    }
                }
                
                // targeted non-living block
                if (!tfTargetFound && target.getType() == TargetType.BLOCK
                        && JojoModUtil.breakingBlocksEnabled(user.level)) {
                    BlockPos blockPos = target.getBlockPos();
                    BlockState blockState = world.getBlockState(blockPos);
                    
                    if (!HamonOrganismInfusion.isBlockLiving(blockState)) {
                        tfTargetFound = true;
                        mobFromBlock(tf, blockPos, blockState, (ServerLevel) world, lifeFormCreated, user);
                        tf.moveTo(blockPos, performer.yRot, 0);
                    }
                }
                
                if (tfTargetFound) {
                    tf.withTransformationTarget(lifeFormCreated)
                    .withDuration(ticks)
                    .withOwner(user);
                    
                    GETransformationData sourceData = tf.getTfSourceData();
                    LivingEntity targetEntity = getLastHurtTarget(user, power.getStandManifestation() instanceof StandEntity ? (StandEntity) power.getStandManifestation() : null);
                    if (targetEntity != null) {
                        UUID targetAlreadySet = sourceData.getFollowTarget();
                        UUID hitTargetId = targetEntity.getUUID();
                        boolean setAggroTarget = targetAlreadySet == null || 
                                targetAlreadySet.equals(hitTargetId) 
                                && sourceData.getFollowTargetMode() != GETransformationEntity.FollowTargetMode.AGGRO_TRACK;
                        if (setAggroTarget) {
                            sourceData.withFollowTarget(targetEntity.getUUID(), GETransformationEntity.FollowTargetMode.AGGRO_FORGETFUL, user);
                        }
                    }
                    
                    GECreatedLifeformEffect effect = new GECreatedLifeformEffect();
                    effect.withStand(power).withTarget(tf);
                    effect.setSource(sourceData);
                    power.getContinuousEffects().addEffect(effect);
                    
                    lifeFormCreated.copyPosition(tf);
                    lifeFormCreated.setYHeadRot(lifeFormCreated.yRot);
                    if (customName.get() != null) {
                        lifeFormCreated.setCustomName(customName.get());
                    }
                    world.addFreshEntity(tf);
                    
                    if (itemTrackerId != null) {
                        StandEffectsTracker.getEffectsOfType(Optional.of(power), ModStandEffects.GE_ITEM_MARK.get())
                        .forEach(StandEffectInstance::remove);
                    }
                    
                    if (lifeFormCreated instanceof LivingEntity) {
                        IStandPower.getStandPowerOptional((LivingEntity) lifeFormCreated).ifPresent(mobStand -> {
                            if (mobStand.getType() == ModStandsInit.MR_PRESIDENT.get()) {
                                LazyOptional<MrPresidentWorldData> mrPresidentTracker = MrPresidentWorldData.get(((ServerLevel) user.level).getServer());
                                mrPresidentTracker.ifPresent(tracker -> {
                                    tracker.rememberTurtlePosition(lifeFormCreated);
                                    List<Entity> entitiesToTeleport = MrPresidentStandType.findTargets(
                                            lifeFormCreated, toTeleport -> 
                                            toTeleport != tf && toTeleport != user && toTeleport != power.getStandManifestation());
                                    if (nonUserItemHolder.get() != null) {
                                        entitiesToTeleport = new ArrayList<>(entitiesToTeleport);
                                        entitiesToTeleport.add(nonUserItemHolder.get());
                                    }
                                    MrPresidentStandType.teleportEntities(lifeFormCreated, mobStand, entitiesToTeleport);
                                });
                            }
                        });
                    }
                    
                    if (!power.isUserCreative()) {
                        int cooldown = Math.max(ticks / 2, 1);
                        tf.actionCooldown = cooldown;
                        power.setCooldownTimer(this, cooldown);
                    }
                }
            }
            else if (user instanceof ServerPlayer) {
                ((ServerPlayer) user).displayClientMessage(Component.translatable("jojo.message.action_condition.choose_lifeform"), true);
            }
        }
    }
    
    public static final Stack<BlockEntity> KEEP_ITEMS = new Stack<>();
    
    @Nullable
    private static LivingEntity getLastHurtTarget(LivingEntity standUser, @Nullable StandEntity standEntity) {
        if (standEntity == null) {
            return standUser.getLastHurtMob();
        }
        
        if (standUser.getLastHurtMob() == null) {
            return standEntity.getLastHurtMob();
        }
        else if (standEntity.getLastHurtMob() == null) {
            return standUser.getLastHurtMob();
        }
        else {
            int hurtByUserTime = standUser.tickCount - standUser.getLastHurtMobTimestamp();
            int hurtByStandTime = standEntity.tickCount - standEntity.getLastHurtMobTimestamp();
            return hurtByUserTime < hurtByStandTime ? standUser.getLastHurtMob() : standEntity.getLastHurtMob();
        }
    }
    
    
    private void mobFromEntity(GETransformationEntity tf, Entity entity, LivingEntity geUser) {
        MCUtil.cloneEntity(entity).ifPresent(e -> tf.getTfSourceData().withEntitySource(e));
        entity.remove();
        
        Vec3 pos = entity.position();
        tf.moveTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
        
        if (entity.isOnFire()) {
            tf.setSecondsOnFire((entity.getRemainingFireTicks() + 19) / 20);
        }
        if (!(entity instanceof AbstractArrow && ((AbstractArrow) entity).inGround)) {
            tf.setDeltaMovement(entity.getDeltaMovement());
        }
        
        if (entity instanceof ItemEntity) {
            UUID thrower = ((ItemEntity) entity).getThrower();
            if (thrower != null) {
                tf.getTfSourceData().withFollowTarget(thrower, GETransformationEntity.FollowTargetMode.TRACK, geUser);
            }
        }
    }
    
    private void mobFromInventory(GETransformationEntity tf, TrackerItemStack itemTracker, Level world, 
            @Nonnull LivingEntity wouldBeThrower, BlockPos fishBucketPos, ObjectWrapper<Component> mobName) {
        Entity holder = itemTracker.getAtEntity(world);
        if (holder instanceof ItemFrame) {
            tf.moveTo(tf.position().add(holder.getLookAngle().scale(0.5)));
        }
        itemTracker.onShrink((ServerLevel) world);
        itemTracker.clear();
        mobFromInventory(tf, itemTracker.getItem(), world, wouldBeThrower, fishBucketPos, mobName);
    }
    
    private void mobFromInventory(GETransformationEntity tf, ItemStack item, Level world, 
            @Nonnull LivingEntity wouldBeThrower, BlockPos fishBucketPos, ObjectWrapper<Component> mobName) {
        Entity itemEntity;
        ItemStack transformedItem = null;
        if (item.getItem() instanceof BucketItem) {
            BucketItem bucketType = (BucketItem) item.getItem();
            Fluid fluid = bucketType.getFluid();
            Item bucketWithoutFish = fluid.getBucket();
            if (bucketWithoutFish != Items.AIR) {
                transformedItem = new ItemStack(bucketWithoutFish);
                bucketType.checkExtraContent(world, item, fishBucketPos);
            }
        }
        if (transformedItem == null) {
            transformedItem = item.copy();
        }
        transformedItem.setCount(1);
        if (item.getItem() instanceof ThrowablePotionItem) {
            ThrownPotion potionEntity = new ThrownPotion(world, wouldBeThrower);
            potionEntity.setItem(transformedItem);
            itemEntity = potionEntity;
        }
        else if (item.getItem() == Items.ENDER_PEARL) {
            ThrownEnderpearl pearlEntity = new ThrownEnderpearl(world, wouldBeThrower);
            itemEntity = pearlEntity;
        }
        else if (item.getItem() == ModItems.MOLOTOV.get() && wouldBeThrower instanceof Player && MolotovItem.useFire((Player) wouldBeThrower, world)) {
            MolotovEntity molotovEntity = new MolotovEntity(world, wouldBeThrower);
            itemEntity = molotovEntity;
        }
        else {
            itemEntity = new ItemEntity(world, 0, 0, 0, transformedItem);
        }
        if (item.hasCustomHoverName()) {
            mobName.set(item.getHoverName());
        }
        item.shrink(1);
        
        tf.getTfSourceData().withEntitySource(itemEntity);
    }
    
    private static final ResourceLocation ENCH_TABLE_ID = new ResourceLocation("minecraft:enchanting_table");
    private void mobFromBlock(GETransformationEntity tf, BlockPos blockPos, BlockState blockState, ServerLevel world, Entity lifeformCreated, LivingEntity geUser) {
        BlockEntity tileEntity = world.getBlockEntity(blockPos);
        if (tileEntity != null) {
            ResourceLocation teId = tileEntity.getType().getRegistryName();
            if (ModInteractionUtil.isModLoaded("apotheosis") && ENCH_TABLE_ID.equals(teId)) {
                tileEntity = null;
            }
        }
        boolean keepItems = tileEntity instanceof Container;
        
        if (keepItems) {
            KEEP_ITEMS.add(tileEntity);
            
            if (lifeformCreated.getType().getRegistryName().getPath().contains("pigeon")) {
                Container inventory = (Container) tileEntity;
                Optional<UUID> deliveryDest = IntStream.range(0, inventory.getMaxStackSize()).mapToObj(inventory::getItem)
                        .filter(item -> !item.isEmpty() && item.getItem() == Items.NAME_TAG && item.hasCustomHoverName())
                        .map(nameTag -> nameTag.getHoverName().getString())
                        .filter(name -> !StringUtils.isBlank(name))
                        .map(name -> {
                            ServerPlayer online = world.getServer().getPlayerList().getPlayerByName(name);
                            if (online != null) {
                                return online.getUUID();
                            }
                            return Player.createPlayerUUID(name);
                        })
                        .filter(id -> id != null).findFirst();
                deliveryDest.ifPresent(destId -> tf.getTfSourceData().withFollowTarget(destId, GETransformationEntity.FollowTargetMode.DELIVERY, geUser));
            }
        }
        world.removeBlock(blockPos, false);
        if (keepItems) {
            KEEP_ITEMS.remove(tileEntity);
        }
        
        tf.getTfSourceData().withBlockSource(blockState, blockPos, tileEntity);
    }
    

    
    static int getTicksToCreate(LivingEntity user, IStandPower power, Entity targetEntity) {
        return getTicksToCreate(user, power, targetEntity, 
                targetEntity.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getMetMobs).orElse(null));
    }
    
    public static int getTicksToCreate(LivingEntity user, IStandPower power, Entity targetEntity, LifeformsMetMobs geUserMetMobs) {
        double entityStrength = getAttackStrength(targetEntity);
        float volume = getVolume(targetEntity);
        double standSpeed = 0;
        if (power != null && power.hasPower()) {
            StandStats stats = power.getType().getStats();
            standSpeed = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(power.getStatsDevelopment());
        }
        
        double value = 240 / Math.max(standSpeed, 1)
                + Mth.ceil(volume * (1 + entityStrength * 0.125) * Mth.clamp(100 - standSpeed * 2, 0, 100));
        if (geUserMetMobs != null && geUserMetMobs.isMobNativeToPlayerPos(user.level, targetEntity, user)) {
            value *= 0.5;
        }
        return (int) value;
    }
    
    
    public float getStaminaCostTicking(IStandPower stand, Entity lifeform) {
        float baseCost = getStaminaCostTicking(stand);
        
        if (lifeform != null) {
            double entityStrength = getAttackStrength(lifeform);
            float volume = getVolume(lifeform);
            
            float entityMultiplier = Mth.clamp(volume, 1, 3);
            if (entityStrength > 0) {
                entityMultiplier *= Mth.clamp(entityStrength, 2, 6) * 0.45 + 0.3;
            }
            
            return baseCost * entityMultiplier;
        }
        
        return baseCost;
    }
    
    public static float getVolume(Entity entity) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        return width * width * height;
    }
    
    public static double getAttackStrength(Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (living.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
                return living.getAttributeValue(Attributes.ATTACK_DAMAGE);
            }
        }
        return 0;
    }
    
    
    public static int getStuckArrows(LivingEntity entity) {
        return entity.getArrowCount();
    }
    
    public static void decrementStuckArrow(LivingEntity entity) {
        entity.setArrowCount(entity.getArrowCount() - 1);
    }
    
    public static int getStuckKnives(LivingEntity entity) {
        return entity.getCapability(LivingUtilCapProvider.CAPABILITY)
                .map(data -> data.getStuckObjects().getKnives().getCount()).orElse(0);
    }
    
    public static void decrementStuckKnife(LivingEntity entity) {
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(data -> data.getStuckObjects().getKnives()).ifPresent(
                knives -> knives.setCount(knives.getCount() - 1));
    }
    
    
    
    @Override
    public MutableComponent getTranslatedName(IStandPower power, String key) {
        EntitySubtype<?> chosenEntityType = getChosenEntityType(ClientUtil.getClientPlayer());
        if (chosenEntityType != null) {
            return Component.translatable(key + ".param", chosenEntityType.getDescription());
        }
        else {
            return super.getTranslatedName(power, key);
        }
    }
    
    @Override
    public void renderActionIcon(PoseStack matrixStack, IStandPower power, float x, float y) {
        EntitySubtype<?> selectedMob = GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer());
        if (selectedMob != null) {
            EntityTypeIcon.renderIcon(selectedMob, matrixStack, x, y);
        }
        else {
            super.renderActionIcon(matrixStack, power, x, y);
        }
    }
}
