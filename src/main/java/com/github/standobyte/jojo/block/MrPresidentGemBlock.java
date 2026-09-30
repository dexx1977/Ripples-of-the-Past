package com.github.standobyte.jojo.block;

import java.util.UUID;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.mrpresident.MrPresidentStandType;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.ChunkSectionPos;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

public class MrPresidentGemBlock extends Block {
    /*
     * 1 - up
     * 2 - right
     * 3 - down
     * 4 - left
     * 5 - left up
     * 6 - right up
     * 7 - right down
     * 8 - left down
     */
    public static final IntegerProperty BORDER_VARIANT = IntegerProperty.create("border_variant", 0, 8);
    
    public MrPresidentGemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BORDER_VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BORDER_VARIANT);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && world.dimension() == ModDimensions.MR_PRESIDENT) {
            if (!world.isClientSide()) {
                MrPresidentWorldData rooms = world.getCapability(MrPresidentWorldDataProvider.CAPABILITY).resolve().get();
                UUID turtleId = rooms.getTurtleId(new ChunkSectionPos(pos));
                if (turtleId != null) {
                    MrPresidentStandType.teleportFromRoom(player, turtleId, world.getServer());
                    return InteractionResult.CONSUME;
                }
            }
        }
        
        return InteractionResult.PASS;
    }
    
}
