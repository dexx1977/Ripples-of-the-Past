package com.github.standobyte.jojo.tileentity;

import net.minecraft.core.BlockPos;
import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModTileEntities;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.SoundSource;

public class StoneMaskTileEntity extends BlockEntity {
    protected ItemStack maskStack = new ItemStack(ModItems.STONE_MASK.get());
    private int activationTicks;
    
    public StoneMaskTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.STONE_MASK.get(), pos, state);
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Item", 10)) {
            maskStack = ItemStack.of(compound.getCompound("Item"));
        }
        activationTicks = compound.getInt("ActivationTicks");
    }
    
    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Item", maskStack.save(new CompoundTag()));
        compound.putInt("ActivationTicks", activationTicks);
    }
    
    @Override
    public void tick() {
        if (activationTicks > 0) {
            activationTicks--;
            setChanged();
            if (activationTicks == 0) {
                level.playSound(null, this.getBlockPos(), ModSounds.STONE_MASK_DEACTIVATION.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StoneMaskBlock.BLOOD_ACTIVATION, false));
            }
        }
    }
    
    public void activate() {
        activationTicks = 100;
        setChanged();
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StoneMaskBlock.BLOOD_ACTIVATION, true));
    }
    
    public boolean isActivated() {
        return activationTicks > 0;
    }
    
    public void setStack(ItemStack stack) {
        this.maskStack = stack;
    }
    
    public ItemStack getStack() {
        return maskStack.copy();
    }
}
