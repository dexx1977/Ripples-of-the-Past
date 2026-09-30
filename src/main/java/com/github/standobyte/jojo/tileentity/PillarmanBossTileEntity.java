package com.github.standobyte.jojo.tileentity;

import com.github.standobyte.jojo.init.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PillarmanBossTileEntity extends BlockEntity {
    private int absorbedLife;
    
    public PillarmanBossTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.SLUMBERING_PILLARMAN.get(), pos, state);
    }

    protected PillarmanBossTileEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
        super(tileEntityType, pos, state);
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        absorbedLife = compound.getInt("AbsorbedLife");
    }
    
    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("AbsorbedLife", absorbedLife);
    }

    public void tick() {
    }
    
    public void incAbsorbed() {
        absorbedLife++;
        setChanged();
        BlockState blockState = this.getBlockState();
        level.sendBlockUpdated(worldPosition, blockState, blockState, 2);
    }
    
    public int getAbsorbedLife() {
        return absorbedLife;
    }
}
