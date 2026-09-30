package com.github.standobyte.jojo.entity.stand.stands;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public boolean attackEntity(Supplier<Boolean> doAttack, StandEntityPunch punch, StandEntityTask task) {
        return DamageUtil.dealDamageAndSetOnFire(punch.target, 
                entity -> super.attackEntity(doAttack, punch, task), 10, true);
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
    
    @Deprecated
    public static void removeFireUnderPlayer(LivingEntity user, IStandPower power) {
        Level world = user.level;
        if (!world.isClientSide() && user.isAlive()
                && power.isActive() && power.getStandManifestation() instanceof MagiciansRedEntity) {
            AABB userHitbox = user.getBoundingBox();
            BlockPos pos1 = BlockPos.containing(userHitbox.minX + 0.001D, userHitbox.minY + 0.001D, userHitbox.minZ + 0.001D);
            BlockPos pos2 = BlockPos.containing(userHitbox.maxX - 0.001D, userHitbox.maxY - 0.001D, userHitbox.maxZ - 0.001D);
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            if (world.hasChunksAt(pos1, pos2)) {
                for(int x = pos1.getX(); x <= pos2.getX(); ++x) {
                    for(int y = pos1.getY(); y <= pos2.getY(); ++y) {
                        for(int z = pos1.getZ(); z <= pos2.getZ(); ++z) {
                            blockPos.set(x, y, z);
                            BlockState blockState = world.getBlockState(blockPos);
                            if (!blockState.isAir() && blockState.getBlock() instanceof BaseFireBlock) {
                                MCUtil.destroyBlock(world, blockPos, false, null);
                            }
                        }
                    }
                }
            }
        }
    }
}
