package com.github.standobyte.jojo.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModTileEntities;
import com.github.standobyte.jojo.tileentity.StoneMaskTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class StoneMaskBlock extends FaceAttachedHorizontalDirectionalBlock implements net.minecraft.world.level.block.EntityBlock { // TODO allow harvesting it with silk touch tool (how do loot tables interact with tile entities tho?)
    public static final DirectionProperty HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty BLOOD_ACTIVATION = BooleanProperty.create("blood_activation");
    protected static final VoxelShape WALL_NORTH_SHAPE = Block.box(4.0D, 4.0D, 15.0D, 12.0D, 12.0D, 16.0D);
    protected static final VoxelShape WALL_SOUTH_SHAPE = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 1.0D);
    protected static final VoxelShape WALL_WEST_SHAPE = Block.box(15.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    protected static final VoxelShape WALL_EAST_SHAPE = Block.box(0.0D, 4.0D, 4.0D, 1.0D, 12.0D, 12.0D);
    protected static final VoxelShape FLOOR_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 1.0D, 12.0D);
    protected static final VoxelShape FLOOR_ACTIVATED_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D);
    protected static final VoxelShape CEILING_SHAPE = Block.box(4.0D, 15.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public StoneMaskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BLOOD_ACTIVATION, false));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACE)) {
        case FLOOR:
            return state.getValue(BLOOD_ACTIVATION) ? FLOOR_ACTIVATED_SHAPE : FLOOR_SHAPE;
        case WALL:
            switch(state.getValue(HORIZONTAL_FACING)) {
            case EAST:
                return WALL_EAST_SHAPE;
            case WEST:
                return WALL_WEST_SHAPE;
            case SOUTH:
                return WALL_SOUTH_SHAPE;
            case NORTH:
            default:
                return WALL_NORTH_SHAPE;
            }
        case CEILING:
        default:
            return CEILING_SHAPE;
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide()) {
            player.addItem(getItemFromBlock(world, pos, state));
            world.removeBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        BlockState blockState = super.updateShape(state, facing, facingState, world, currentPos, facingPos);
        if (blockState == Blocks.AIR.defaultBlockState() && !world.isClientSide() && world instanceof Level) {
            Block.popResource((Level) world, currentPos, StoneMaskBlock.getItemFromBlock(world, currentPos, blockState));
        }
        return blockState;
    }
    
    public static ItemStack getItemFromBlock(BlockGetter world, BlockPos pos, BlockState state) {
        ItemStack stack = ItemStack.EMPTY;
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof StoneMaskTileEntity) {
            stack = ((StoneMaskTileEntity) tileEntity).getStack();
        }
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, HORIZONTAL_FACING, BLOOD_ACTIVATION);
    }
    
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof StoneMaskTileEntity) {
            ((StoneMaskTileEntity) tileEntity).setStack(stack.copy());
        }
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModTileEntities.STONE_MASK.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModTileEntities.STONE_MASK.get()
                ? (lvl, pos, st, blockEntity) -> ((StoneMaskTileEntity) blockEntity).tick()
                : null;
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity tileEntity, ItemStack itemUsed) {
        super.playerDestroy(world, player, blockPos, blockState, tileEntity, itemUsed);
        if (!world.isClientSide && tileEntity instanceof StoneMaskTileEntity) {
            StoneMaskTileEntity stoneMask = (StoneMaskTileEntity) tileEntity;

            ModCriteriaTriggers.STONE_MASK_DESTROYED.get().trigger((ServerPlayer) player, 
                    blockState.getBlock(), itemUsed, stoneMask.getStack());
        }
    }
}
