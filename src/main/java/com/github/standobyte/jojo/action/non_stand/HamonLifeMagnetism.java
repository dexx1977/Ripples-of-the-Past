package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkAccess;

public class HamonLifeMagnetism extends HamonAction {

    public HamonLifeMagnetism(HamonAction.Builder builder) {
        super(builder.needsFreeMainHand());
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (target.getType() == TargetType.BLOCK && user.level.getBlockState(target.getBlockPos()).getBlock() instanceof LeavesBlock
                ||
            useItemForGlider(user.getMainHandItem()) || useItemForGlider(user.getOffhandItem()) 
                ||
            user instanceof Player && !MCUtil.findInInventory(((Player) user).inventory, 
                    item -> !item.isEmpty() && item.getItem() instanceof BlockItem && 
                    ((BlockItem) item.getItem()).getBlock() instanceof LeavesBlock).isEmpty()) {
            return ActionConditionResult.POSITIVE;
        }
        return conditionMessage("leaves");
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (useItemForGlider(user.getMainHandItem()) || useItemForGlider(user.getOffhandItem())) {
            return ActionConditionResult.POSITIVE;
        }
        return super.checkHeldItems(user, power);
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            if (target.getType() == TargetType.BLOCK) {
                BlockPos blockPos = target.getBlockPos();
                BlockState blockState = user.level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof LeavesBlock) {
                    MCUtil.destroyBlock(world, blockPos, false, null);
                    LeavesGliderEntity glider = summonGlider(world, user, power, Vec3.atBottomCenterOf(blockPos), false, blockState);
                    
                    ChunkAccess chunk = world.getChunk(blockPos);
                    if (chunk instanceof LevelChunk) {
                        ((LevelChunk) chunk).getCapability(ChunkCapProvider.CAPABILITY).ifPresent(cap -> {
                            PrevBlockInfo brokenBlock = cap.getBrokenBlockAt(blockPos);
                            if (brokenBlock != null) {
                                brokenBlock.withEntities(glider);
                            }
                        });
                    }
                    
                    return;
                }
            }
            
            if (user instanceof Player) {
                Player player = (Player) user;
                Container inventory = player.inventory;
                ItemStack leavesItem = user.getMainHandItem();
                if (!useItemForGlider(leavesItem)) leavesItem = user.getOffhandItem();
                if (!useItemForGlider(leavesItem)) leavesItem = MCUtil.findInInventory(inventory, item -> useItemForGlider(item));
                if (!leavesItem.isEmpty()) {
                    summonGlider(world, user, power, user.position(), true, ((BlockItem) leavesItem.getItem()).getBlock().defaultBlockState());
                    if (!player.abilities.instabuild) {
                        leavesItem.shrink(1);
                    }
                }
            }
        }
    }
    
    private boolean useItemForGlider(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof BlockItem && 
                ((BlockItem) item.getItem()).getBlock() instanceof LeavesBlock;
    }
    
    private LeavesGliderEntity summonGlider(Level world, LivingEntity user, INonStandPower power, Vec3 pos, boolean mount, BlockState leavesBlock) {
        LeavesGliderEntity glider = new LeavesGliderEntity(world);
        glider.moveTo(pos.x, pos.y, pos.z, user.xRot, user.yRot);
        glider.setLeavesBlock(leavesBlock);
        world.addFreshEntity(glider);
        if (mount) {
            user.startRiding(glider);
        }
        glider.setEnergy(Math.min(power.getEnergy(), LeavesGliderEntity.MAX_ENERGY));
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost(power, ActionTarget.EMPTY));
        HamonUtil.emitHamonSparkParticles(world, null, pos.x, glider.getY(1.0F), pos.z, 0.1F);
        return glider;
    }
    
    @Override
    public boolean renderHamonAuraOnItem(ItemStack item, HumanoidArm handSide) {
        return item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof LeavesBlock;
    }
}
