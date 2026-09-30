package com.github.standobyte.jojo.tileentity;

import com.github.standobyte.jojo.init.ModTileEntities;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PillarmanBossTileEntity extends BlockEntity implements ITickableTileEntity {
    private int absorbedLife;
    
    public PillarmanBossTileEntity() {
        super(ModTileEntities.SLUMBERING_PILLARMAN.get());
    }

    protected PillarmanBossTileEntity(BlockEntityType<?> tileEntityType) {
        super(tileEntityType);
    }
    
    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        absorbedLife = compound.getInt("AbsorbedLife");
    }
    
    @Override
    public CompoundTag save(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("AbsorbedLife", absorbedLife);
        return compound;
    }

    @Override
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
