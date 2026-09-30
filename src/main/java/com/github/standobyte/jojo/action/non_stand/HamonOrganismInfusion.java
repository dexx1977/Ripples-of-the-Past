package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.google.common.collect.ImmutableSet;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.block.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import com.github.standobyte.jojo.util.mc.MCUtil;

public class HamonOrganismInfusion extends HamonAction {

    public HamonOrganismInfusion(HamonAction.Builder builder) {
        super(builder);
    }

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            Entity entity = target.getEntity();
            boolean isLiving;
            if (entity instanceof LivingEntity) {
                LivingEntity targetLiving = (LivingEntity) entity;
                isLiving = HamonUtil.isLiving(targetLiving);
            }
            else {
                isLiving = false;
            }
            if (!isLiving) {
                return conditionMessage("living_mob");
            }
            return super.checkTarget(target, user, power);
        case BLOCK:
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = user.level.getBlockState(blockPos);
            return canChargeBlock(blockPos, blockState, user.level);
        default:
            return ActionConditionResult.NEGATIVE;
        }
    }
    
    private ActionConditionResult canChargeBlock(BlockPos blockPos, BlockState blockState, Level world) {
        Block block = blockState.getBlock();
        boolean isLivingBlock;
        if (isBlockLiving(blockState)) {
            isLivingBlock = true;
        }
        else if (block instanceof FlowerPotBlock && blockState.getBlock() != Blocks.FLOWER_POT) {
            FlowerPotBlock flowerPot = (FlowerPotBlock) block;
            ItemStack flowerPotContents = flowerPot.getCloneItemStack(world, blockPos, blockState);
            isLivingBlock = HamonUtil.isItemLivingMatter(flowerPotContents);
        }
        else {
            isLivingBlock = false;
        }
        if (!isLivingBlock) {
            return conditionMessage("living_plant");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY;
    }
    
    @Override
    public float getEnergyCost(INonStandPower power, ActionTarget target) {
        if (this == ModHamonActions.HAMON_PLANT_INFUSION.get() || target.getType() == TargetType.ENTITY) {
            return super.getEnergyCost(power, target);
        }
        return ModHamonActions.HAMON_PLANT_INFUSION.get().getEnergyCost(power, target);
    }
    
    @Override
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, Level world, LivingEntity user, INonStandPower power) {
        if (getTargetRequirement().checkTargetType(TargetType.ENTITY) && targetContainer.get().getType() == TargetType.BLOCK) {
            BlockPos blockPos = targetContainer.get().getBlockPos();
            VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
            if (shape.isEmpty()) {
                targetContainer.set(ActionTarget.EMPTY);
                return;
            }
            Optional<Entity> entityInside = world.getEntities(user, shape.bounds().move(blockPos))
                    .stream()
                    .filter(entity -> (entity instanceof Animal || entity instanceof AmbientCreature)
                            && entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).map(cap -> !cap.hasHamonCharge()).orElse(false))
                    .findAny();
            if (entityInside.isPresent()) {
                targetContainer.set(new ActionTarget(entityInside.get()));
            }
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            
            float hamonEfficiency = hamon.getActionEfficiency(getEnergyCost(power, target), true, getUnlockingSkill());
            int chargeTicks = 100 + Mth.floor((float) (1100 * hamon.getHamonStrengthLevel())
                    / (float) HamonData.MAX_STAT_LEVEL * hamonEfficiency * hamonEfficiency);
            switch (target.getType()) {
            case BLOCK:
                BlockPos blockPos = target.getBlockPos();
                addBlockCharge(user.level, blockPos, power, user, hamon, hamonEfficiency, chargeTicks);
                if (hamon.isSkillLearned(ModHamonSkills.HAMON_SPREAD.get())) {
                    addBlockCharge(user.level, blockPos.offset(-1,  0,  0), power, user, hamon, hamonEfficiency, chargeTicks);
                    addBlockCharge(user.level, blockPos.offset( 1,  0,  0), power, user, hamon, hamonEfficiency, chargeTicks);
                    addBlockCharge(user.level, blockPos.offset( 0, -1,  0), power, user, hamon, hamonEfficiency, chargeTicks);
                    addBlockCharge(user.level, blockPos.offset( 0,  1,  0), power, user, hamon, hamonEfficiency, chargeTicks);
                    addBlockCharge(user.level, blockPos.offset( 0,  0, -1), power, user, hamon, hamonEfficiency, chargeTicks);
                    addBlockCharge(user.level, blockPos.offset( 0,  0,  1), power, user, hamon, hamonEfficiency, chargeTicks);
                }
                break;
            case ENTITY:
                LivingEntity entity = (LivingEntity) target.getEntity();
                entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
	                if (!cap.hasHamonCharge()) {
	                	cap.setHamonCharge(hamon.getHamonDamageMultiplier() * hamonEfficiency, chargeTicks, user, getEnergyCost(power, target));
	                }
                });
                break;
            default:
                break;
            }
        }
    }
    
    private void addBlockCharge(Level world, BlockPos blockPos, INonStandPower power, LivingEntity user, HamonData hamon, float hamonEfficiency, int chargeTicks) {
        BlockState blockState = world.getBlockState(blockPos);
        if (!canChargeBlock(blockPos, blockState, world).isPositive()) {
            return;
        }
        world.getEntitiesOfClass(HamonBlockChargeEntity.class, 
                new AABB(Vec3.atCenterOf(blockPos), Vec3.atCenterOf(blockPos))).forEach(Entity::remove);
        HamonBlockChargeEntity charge = new HamonBlockChargeEntity(world, blockPos);
        charge.setCharge(hamon.getHamonDamageMultiplier() * hamonEfficiency, chargeTicks, user, getEnergyCost(power, new ActionTarget(blockPos, Direction.UP)));
        world.addFreshEntity(charge);
    }

    private static Set<ResourceLocation> otherLivingBlocksCache;
    private static Set<ResourceLocation> exceptionBlocksCache;
    public static boolean isBlockLiving(BlockState blockState) {
        if (otherLivingBlocksCache == null) {
            exceptionBlocksCache = ForgeRegistries.BLOCKS.getValues().stream()
                    .map(Block::getRegistryName)
                    .filter(id -> {
                        String blockName = id.getPath();
                        return blockName.contains("dead");
                    })
                    .collect(Collectors.toSet());
            otherLivingBlocksCache = ForgeRegistries.BLOCKS.getValues().stream()
                    .map(Block::getRegistryName)
                    .filter(id -> {
                        String blockName = id.getPath();
                        return !exceptionBlocksCache.contains(id) && (blockName.contains("mossy") || blockName.contains("coral"));
                    })
                    .collect(Collectors.toSet());
        }
        
        Block block = blockState.getBlock();
        ResourceLocation id = MCUtil.id(block);
        
        if (MCUtil.isReplaceablePlant(blockState)) {
            return !exceptionBlocksCache.contains(id);
        }
        // 1.16.5 asked the block's Material here; 1.20.1 has no Material, so the
        // same living/organic groups are recognised from the block's sound type and
        // the vanilla plant/coral/leaf tags (see MCUtil.isLivingBlockMaterial).
        return MCUtil.isLivingBlockMaterial(blockState) || otherLivingBlocksCache.contains(id);
    }

}