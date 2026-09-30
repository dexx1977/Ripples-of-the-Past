package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.MolotovEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class MolotovItem extends Item {

    public MolotovItem(Properties pProperties) {
        super(pProperties);

        DispenserBlock.registerBehavior(this, new DispenseItemBehavior() {
            public ItemStack dispense(BlockSource blockSource, ItemStack item) {
                return new AbstractProjectileDispenseBehavior() {
                    
                    @Override
                    protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack) {
                        return new MolotovEntity(pLevel, pPosition.x(), pPosition.y(), pPosition.z());
                    }
                    
                    @Override
                    protected float getUncertainty() {
                        return super.getUncertainty() * 0.5F;
                    }
                    
                    @Override
                    protected float getPower() {
                        return super.getPower() * 1.25F;
                    }
                }.dispense(blockSource, item);
            }
        });
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        
        boolean hasFire = useFire(player, world);
        if (!hasFire) {
            if (!world.isClientSide()) {
                player.displayClientMessage(Component.translatable("jojo.message.action_condition.molotov_fire"), true);
            }
            return InteractionResultHolder.fail(heldItem);
        }
        
        if (!world.isClientSide) {
            MolotovEntity molotovEntity = new MolotovEntity(world, player);
            molotovEntity.setItem(heldItem);
            molotovEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 0.75F, 1.0F);
            world.addFreshEntity(molotovEntity);
            if (!player.abilities.instabuild) {
                heldItem.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        if (!player.isSilent()) {
            player.playSound(ModSounds.MOLOTOV_THROW.get(), 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        }
        
        return InteractionResultHolder.sidedSuccess(heldItem, world.isClientSide());
    }
    
    public static boolean useFire(Player player, Level world) {
        boolean hasFire = player.isOnFire() || IStandPower.getStandPowerOptional(player).resolve()
                .map(stand -> stand.getType() == ModStands.MAGICIANS_RED.getStandType()).orElse(false);
        ObjectWrapper<ItemStack> flintAndSteelWr = new ObjectWrapper<>(ItemStack.EMPTY);
        ItemStack fireCharge = ItemStack.EMPTY;
        if (!hasFire) {
            BlockPos center = player.blockPosition().above();
            for (int x = -2; x <= 2 && !hasFire; x++) {
                for (int y = -2; y <= 2 && !hasFire; y++) {
                    for (int z = -2; z <= 2 && !hasFire; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState blockState = world.getBlockState(pos);
                        FluidState fluidState = world.getFluidState(pos);
                        if (
                                blockState.getBlock() instanceof BaseFireBlock || 
                                    ((blockState.getBlock() instanceof AbstractFurnaceBlock || blockState.getBlock() instanceof CampfireBlock
                                            || blockState.getBlock() instanceof TorchBlock && blockState.getBlock() != Blocks.REDSTONE_TORCH)
                                     && (!blockState.hasProperty(BlockStateProperties.LIT) || blockState.getValue(BlockStateProperties.LIT))) || 
                                fluidState.getType() instanceof LavaFluid || 
                                fluidState.is(FluidTags.LAVA)) {
                            hasFire = true;
                        }
                    }
                }
            }
        }
        if (!hasFire) {
            flintAndSteelWr.set(MCUtil.findInInventory(player.inventory, 
                    stack -> !stack.isEmpty() && stack.getItem() instanceof FlintAndSteelItem));
            if (!flintAndSteelWr.get().isEmpty()) {
                hasFire = true;
                if (!player.isSilent()) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.FLINTANDSTEEL_USE, player.getSoundSource(), 1, random.nextFloat() * 0.4F + 0.8F);
                }
            }
        }
        if (!hasFire) {
            fireCharge = MCUtil.findInInventory(player.inventory, 
                    stack -> !stack.isEmpty() && stack.getItem() instanceof FireChargeItem);
            if (!fireCharge.isEmpty()) {
                hasFire = true;
                if (!player.isSilent()) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.FIRECHARGE_USE, player.getSoundSource(), 1, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                }
            }
        }
        
        if (hasFire) {
            ItemStack flintAndSteel = flintAndSteelWr.get();
            if (!flintAndSteel.isEmpty()) {
                flintAndSteel.hurtAndBreak(1, player, entity -> {
                    if (!entity.isSilent()) {
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                                SoundEvents.ITEM_BREAK, entity.getSoundSource(), 0.8F, 0.8F + world.random.nextFloat() * 0.4F);
                        MCUtil.spawnItemParticles(player, flintAndSteel, 5);
                    }
                });
            }
            else if (!fireCharge.isEmpty()) {
                fireCharge.shrink(1);
            }
        }
        
        return hasFire;
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("ArchLunatic"));
    }

}
