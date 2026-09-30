package com.github.standobyte.jojo.block;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModTileEntities;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.tileentity.PillarmanBossTileEntity;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class PillarmanBossMultiBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    private static final int PART_WITH_TILE_ENTITY = 4;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty PART = IntegerProperty.create("pillarman_part", 0, 5);
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public PillarmanBossMultiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, PART_WITH_TILE_ENTITY));
    }
    
    /*   i->
     * j 01
     * | 23
     * v 45
     */
    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        int multiBlockPart = state.getValue(PART);
        Direction right = rightDirection(state);
        BlockPos leftUpPos = pos.relative(right, -(multiBlockPart % 2)).relative(Direction.UP, multiBlockPart / 2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (j * 2 + i != multiBlockPart) {
                    BlockPos blockPos = leftUpPos.relative(right, i).relative(Direction.DOWN, j);
                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.getBlock() == this && blockState.getValue(PART) == j * 2 + i) {
                        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 35);
                        world.levelEvent(player, 2001, blockPos, Block.getId(blockState));
                    }
                }
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }
    
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        if (blockpos.getY() < 254) {
            Level world = context.getLevel();
            Direction right = context.getHorizontalDirection().getClockWise();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!world.getBlockState(blockpos.above(j).relative(right, i)).canBeReplaced(context)) {
                        return null;
                    }
                }
            }
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockPos up1 = pos.above();
        BlockPos up2 = up1.above();
        world.setBlock(up1, state.setValue(PART, 2), 3);
        world.setBlock(up2, state.setValue(PART, 0), 3);
        Direction right = rightDirection(state);
        world.setBlock(pos.relative(right), state.setValue(PART, 5), 3);
        world.setBlock(up1.relative(right), state.setValue(PART, 3), 3);
        world.setBlock(up2.relative(right), state.setValue(PART, 1), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }
    
    @Deprecated
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACING)) {
        case EAST:
            return EAST_SHAPE;
        case WEST:
            return WEST_SHAPE;
        case SOUTH:
            return SOUTH_SHAPE;
        case NORTH:
        default:
            return NORTH_SHAPE;
        }
    }
    
    private static final float DAMAGE_AMOUNT = 4;
    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide()) {
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (HamonUtil.preventBlockDamage(livingEntity, world, 
                        pos, state, DamageUtil.damageSource(world, DamageUtil.PILLAR_MAN_ABSORPTION), DAMAGE_AMOUNT)) {
                    return;
                }
                
                if (DamageUtil.dealPillarmanAbsorptionDamage(livingEntity, DAMAGE_AMOUNT, null)) {
                    livingEntity.addEffect(new MobEffectInstance(ModStatusEffects.STUN.get(), 10, 0));
                    BlockPos tileEntityPos = posByPart(state, pos, PART_WITH_TILE_ENTITY);
                    BlockEntity tileEntity = world.getBlockEntity(tileEntityPos);
                    if (tileEntity instanceof PillarmanBossTileEntity) {
                        ((PillarmanBossTileEntity) tileEntity).incAbsorbed();
                    }
                }
            }
        }
    }
    
    private BlockPos posByPart(BlockState state, BlockPos startingPos, int desiredPart) {
        int startingPart = state.getValue(PART);
        int downOffset = desiredPart / 2 - startingPart / 2;
        int rightOffset = desiredPart % 2 - startingPart % 2;
        if (rightOffset != 0) {
            Direction right = rightDirection(state);
            return startingPos.relative(right, rightOffset).relative(Direction.DOWN, downOffset);
        }
        return startingPos.relative(Direction.DOWN, downOffset);
    }
    
    private Direction rightDirection(BlockState state) {
        return state.getValue(FACING).getClockWise();
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModTileEntities.SLUMBERING_PILLARMAN.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModTileEntities.SLUMBERING_PILLARMAN.get()
                ? (lvl, pos, st, blockEntity) -> ((PillarmanBossTileEntity) blockEntity).tick()
                : null;
    }
}
