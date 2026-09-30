package com.github.standobyte.jojo.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class MeteoricOreBlock extends DropExperienceBlock {

    public MeteoricOreBlock(Properties properties) {
        // the experience range the old xpOnDrop override returned
        super(properties, net.minecraft.util.valueproviders.UniformInt.of(6, 10));
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, net.minecraft.util.RandomSource rand) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, (new AABB(x, y, z, x, y, z)).inflate(2.0D, 2.0D, 2.0D))) {
            if (entity.getMobType() != MobType.UNDEAD && entity.getHealth() < entity.getMaxHealth() && !isImmuneToMeteoriteStrain(entity)) {
                entity.hurt(DamageUtil.damageSource(entity, DamageUtil.STAND_VIRUS_METEORITE), 4.0F);
            }
        }
        world.scheduleTick(pos, this, 10);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, net.minecraft.util.RandomSource random) {
        double d0 = (double)((float)pos.getX() + random.nextFloat() * 4F - 2F);
        double d1 = (double)((float)pos.getY() + random.nextFloat() * 4F - 2F);
        double d2 = (double)((float)pos.getZ() + random.nextFloat() * 4F - 2F);
        world.addParticle(ModParticles.METEORITE_VIRUS.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Deprecated
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        world.scheduleTick(currentPos, this, 10);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        world.scheduleTick(pos, this, 10);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, tileEntity, stack);
        if (player.getHealth() < player.getMaxHealth() && !isImmuneToMeteoriteStrain(player)) {
            player.hurt(DamageUtil.damageSource(player, DamageUtil.STAND_VIRUS_METEORITE), 10.0F);
        }
    }
    
    public static boolean isImmuneToMeteoriteStrain(LivingEntity entity) {
        return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(entity), power -> power.hadAnyStand() || power.hasPower());
    }
}
