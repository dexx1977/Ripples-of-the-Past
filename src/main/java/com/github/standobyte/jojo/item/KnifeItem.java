package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.potion.BleedingEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolActions;

public class KnifeItem extends Item {
    // 1.16.5's Item exposed a shared Random; 1.20.1 items carry their own.
    protected static final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();

    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public KnifeItem(Properties properties) {
        super(properties);
        
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 2.0D, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();

        DispenserBlock.registerBehavior(this, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
                KnifeEntity knife = new KnifeEntity(world, position.x(), position.y(), position.z());
                knife.pickup = AbstractArrow.Pickup.ALLOWED;
                return knife;
            }
        });
    }

    public static final int MAX_KNIVES_THROW = 8;
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        int knivesToThrow = !player.isShiftKeyDown() ? Math.min(handStack.getCount(), MAX_KNIVES_THROW) : 1;
        if (!world.isClientSide()) {
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (handStack.getCount() == 1 && headStack.getItem() instanceof StoneMaskItem && BleedingEffect.applyStoneMask(player, headStack)) {
                player.hurt(DamageSource.playerAttack(player), 1.0F);
                return InteractionResultHolder.consume(handStack);
            }
            
            for (int i = 0; i < knivesToThrow; i++) {
                KnifeEntity knifeEntity = new KnifeEntity(world, player);
                knifeEntity.setTimeStopFlightTicks(5);
                knifeEntity.shootFromRotation(player, 1.5F, i == 0 ? 1.0F : 16.0F);
                
                TrackerItemStack.getItemTracker(handStack).ifPresent(tracker -> {
                    if (tracker.isTracked()) {
                        tracker.setAtEntity(knifeEntity.getId(), world, KnownItemState.ENTITY_IS_ITEM);
                        tracker.setItemStillThereCheck(null);
                        knifeEntity.saveItemTrackerNBT(tracker.toNBT());
                    }
                });
                
                world.addFreshEntity(knifeEntity);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    knivesToThrow == 1 ? ModSounds.KNIFE_THROW.get() : ModSounds.KNIVES_THROW.get(), 
                            SoundSource.PLAYERS, 0 * 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            
            int cooldown = knivesToThrow * 3;
            player.getCooldowns().addCooldown(this, cooldown);
            if (!player.abilities.instabuild) {
                handStack.shrink(knivesToThrow);
            }
            
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.getStandManifestation() instanceof StandEntity) {
                    ((StandEntity) power.getStandManifestation()).onKnivesThrow(world, player, handStack, knivesToThrow);
                }
            });
        }
        return InteractionResultHolder.success(handStack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (stack.getCount() > 1) {
            return super.getDestroySpeed(stack, state);
        }
        Block block = state.getBlock();
        if (block == Blocks.COBWEB) {
            return 15.0F;
        } else {
            return !MCUtil.isPlantLike(state) ? 1.0F : 1.5F;
        }
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockIn) {
        return blockIn.getBlock() == Blocks.COBWEB;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && stack.getCount() == 1) {
            return attributeModifiers;
        }
        return super.getAttributeModifiers(slot, stack);
    }
    
    
    // axes are in shambles rn
    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level world = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState block = blockstate.getToolModifiedState(pContext, ToolActions.AXE_STRIP, false);
        if (block != null) {
            Player playerentity = pContext.getPlayer();
            world.playSound(playerentity, blockpos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide) {
                world.setBlock(blockpos, block, 11);
//                if (playerentity != null) {
//                    pContext.getItemInHand().hurtAndBreak(1, playerentity, (p_220040_1_) -> {
//                        p_220040_1_.broadcastBreakEvent(pContext.getHand());
//                    });
//                }
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
    
    // this would actually nerf shears though so i don't feel like going through with this suggestion
//    @Override
//    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity entity, Hand hand) {
//        if (entity.level.isClientSide) return ActionResultType.PASS;
//        if (entity instanceof IForgeShearable) {
//            IForgeShearable target = (IForgeShearable)entity;
//            BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
//            if (target.isShearable(stack, entity.level, pos)) {
//                List<ItemStack> drops = target.onSheared(playerIn, stack, entity.level, pos,
//                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack));
//                Random rand = new Random();
//                drops.forEach(d -> {
//                    ItemEntity ent = entity.spawnAtLocation(d, 1.0F);
//                    ent.setDeltaMovement(ent.getDeltaMovement().add(
//                            (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F), 
//                            (double)(rand.nextFloat() * 0.05F), 
//                            (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
//                });
////                stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(hand));
//            }
//            return ActionResultType.SUCCESS;
//        }
//        return ActionResultType.PASS;
//    }
}
