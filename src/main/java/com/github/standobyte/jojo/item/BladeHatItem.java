package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class BladeHatItem extends CustomModelArmorItem {
    
    public BladeHatItem(ArmorMaterial material, EquipmentSlot slot, Properties builder) {
        super(material, slot, builder);

        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                return ArmorItem.dispenseArmor(blockSource, stack) ? stack : shootProjectile(blockSource, stack);
            }
            
            private ItemStack shootProjectile(BlockSource blockSource, ItemStack itemStack) {
                Level world = blockSource.getLevel();
                Position position = DispenserBlock.getDispensePosition(blockSource);
                Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                BladeHatEntity hat = new BladeHatEntity(world, position.x(), position.y(), position.z(), itemStack.copy());
                hat.pickup = AbstractArrow.Pickup.ALLOWED;
                hat.shoot(direction.getStepX(), direction.getStepY() + 0.1, direction.getStepZ(), 1.1F, 6.0F);
                world.addFreshEntity(hat);
                itemStack.shrink(1);
                return itemStack;
            }
        });
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(world, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            BladeHatEntity hat = new BladeHatEntity(world, player, stack.copy());
            hat.shootFromRotation(player, 0.75F, 0.5F);
            
            TrackerItemStack.getItemTracker(stack).ifPresent(tracker -> {
                if (tracker.isTracked()) {
                    tracker.setAtEntity(hat.getId(), world, KnownItemState.ENTITY_IS_ITEM);
                    tracker.setItemStillThereCheck(null);
                }
            });
            
            world.addFreshEntity(hat);
        }
        player.playSound(ModSounds.BLADE_HAT_THROW.get(), 1.0F, 0.75F + random.nextFloat() * 0.5F);
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.SHARPNESS;
    }
}
