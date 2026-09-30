package com.github.standobyte.jojo.item;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.command.configpack.standassign.PlayerStandAssignmentConfig;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEnchantments;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandArrowHandler;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.StandUtil.StandRandomPoolFilter;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.mojang.datafixers.util.Either;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public class StandArrowItem extends ArrowItem {
    private final int enchantability;
    private final boolean higherDurability;

    public StandArrowItem(Properties properties, int enchantability, boolean higherDurability) {
        super(properties);
        this.enchantability = enchantability;
        this.higherDurability = higherDurability;
        
        DispenserBlock.registerBehavior(this, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
                StandArrowEntity arrow = new StandArrowEntity(world, position.x(), position.y(), position.z(), stack);
                arrow.pickup = AbstractArrow.PickupStatus.ALLOWED;
                return arrow;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!world.isClientSide() && onPiercedByArrow(player, stack, world, Optional.empty())) {
            player.hurt(DamageSource.playerAttack(player), Math.min(1.0F, Math.max(player.getHealth() - 1.0F, 0)));
            stack.hurtAndBreak(1, player, pl -> {});
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
       return new StandArrowEntity(world, shooter, stack);
    }
    
    /** 
     * @return  if the entity got the Stand Virus effect or a Stand
     */
    public static boolean onPiercedByArrow(Entity target, ItemStack stack, Level world, Optional<Entity> arrowShooter) {
        if (!world.isClientSide() && target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;
            if (livingEntity.hasEffect(ModStatusEffects.STAND_VIRUS.get())) {
                return false;
            }
            
            if (livingEntity instanceof StandEntity) {
                return false;
            }
            else if (livingEntity instanceof Player) {
                Player player = (Player) livingEntity;
                return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(livingEntity), standCap -> {
                    Either<StandType<?>, Component> standOrError;
                    if (standCap.hasPower()) {
                        standOrError = Either.right(Component.translatable("jojo.chat.message.already_have_stand"));
                    }
                    else {
                        standOrError = StandUtil.randomStandOrError(player, player.getRandom());
                    }
                    return GeneralUtil.merge(standOrError.mapBoth(
                            standToGive -> {
                                if (player.abilities.instabuild) { // instantly give a stand in creative
                                    return giveStandFromArrow(player, standCap, standToGive);
                                }
                                else {
                                    standCap.getStandArrowHandler().startArrowEffectSetStand(standToGive);

                                    int virusEffectDuration = StandVirusEffect.getEffectDurationToApply(player);
                                    if (virusEffectDuration > 0) {
                                        int inhibitionLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), stack);
                                        int effectLevel = StandVirusEffect.getEffectLevelToApply(inhibitionLevel);
                                        player.addEffect(new MobEffectInstance(ModStatusEffects.STAND_VIRUS.get(), 
                                                virusEffectDuration, effectLevel, false, false, true));
                                    }
                                    else { // instantly give a stand if there was no stand virus effect given
                                        return giveStandFromArrow(player, standCap, standToGive);
                                    }

                                    rememberArrowShooter(livingEntity, arrowShooter, stack);
                                }

                                return true;
                            }, error -> {
                                player.displayClientMessage(error, true);
                                return false;
                            }));
                });
            }
            else {
                int inhibitionLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), stack);
                int effectLevel = StandVirusEffect.getEffectLevelToApply(inhibitionLevel);
                livingEntity.addEffect(new MobEffectInstance(ModStatusEffects.STAND_VIRUS.get(), 
                        600, effectLevel, false, false, true));
                rememberArrowShooter(livingEntity, arrowShooter, stack);
            }
        }
        return false;
    }
    
    private static void rememberArrowShooter(LivingEntity target, Optional<Entity> arrowShooter, ItemStack arrow) {
        IStandPower.getStandPowerOptional(target).ifPresent(power -> {
            StandArrowHandler handler = power.getStandArrowHandler();
            arrowShooter.ifPresent(shooter -> {
                handler.setStandArrowShooter(shooter);
            });
            handler.setStandArrowItem(arrow);
        });
    }
    
    public static boolean giveStandFromArrow(LivingEntity entity, IStandPower standCap, StandType<?> standType) {
        if (standCap.givePower(standType)) {
            StandArrowHandler handler = standCap.getStandArrowHandler();
            handler.onGettingStandFromArrow(entity);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        Player player = ClientUtil.getClientPlayer();
        if (player != null) {
            MutableComponent mainText = null;
            
            Collection<StandType<?>> unbannedStands = StandUtil.arrowStands(true)
                    .sorted(Comparator.comparingInt(stand -> JojoCustomRegistries.STANDS.getNumericId(stand.getRegistryName())))
                    .collect(Collectors.toList());
            
            if (unbannedStands.isEmpty()) {
                mainText = Component.translatable("jojo.arrow.no_stands").withStyle(ChatFormatting.GRAY, ChatFormatting.OBFUSCATED);
                tooltip.add(mainText);
            }
            else {
                StandRandomPoolFilter poolFilter = JojoModConfig.getCommonConfigInstance(true).standRandomPoolFilter.get();
                boolean shift = ClientUtil.isShiftPressed();
                
                if (shift) {
                    tooltip.add(Component.translatable("jojo.arrow.stands_list"));

                    List<StandType<?>> assignedByDataConfig = PlayerStandAssignmentConfig.getInstance().getAssignedStands(player);
                    
                    IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                        unbannedStands.forEach(stand -> {
                            MutableComponent standName = stand.getName();
                            
                            if (assignedByDataConfig != null && !assignedByDataConfig.contains(stand)) {
                                standName.withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.DARK_GRAY);
                            }
                            else {
                                standName.withStyle(ChatFormatting.GRAY);
                            }
                            tooltip.add(standName);
                        });
                        
                        tooltip.add(Component.empty());
                    });
                }
                else {
                    tooltip.add(Component.translatable("jojo.arrow.stands_hint", Component.keybind("key.sneak")));
                }
                
                if (!player.abilities.instabuild) {
                    int levelsNeeded = IStandPower.getStandPowerOptional(player).map(power -> {
                        StandArrowHandler handler = power.getStandArrowHandler();
                        return handler.getStandXpLevelsRequirement(true, stack);
                    }).orElse(0);
                    if (levelsNeeded > 0) {
                        boolean playerHasStand = StandUtil.isEntityStandUser(player);
                        boolean playerHasLevels = player.experienceLevel >= levelsNeeded;
                        tooltip.add(Component.translatable("jojo.arrow.stand_arrow_xp", levelsNeeded).withStyle(
                                playerHasStand ? ChatFormatting.DARK_GRAY : playerHasLevels ? ChatFormatting.GREEN : ChatFormatting.RED));
                    }
                }
                
                boolean isOnServer = !ClientUtil.isInSinglePlayer();
                if (isOnServer) {
                    switch (poolFilter) {
                    case LEAST_TAKEN:
                        tooltip.add(Component.translatable("jojo.arrow.least_taken_mode").withStyle(ChatFormatting.GRAY));
                        break;
                    case NOT_TAKEN: 
                        tooltip.add(Component.translatable("jojo.arrow.not_taken_mode").withStyle(ChatFormatting.GRAY));
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment)
                || enchantment == Enchantments.LOYALTY
                || enchantment == Enchantments.SHARPNESS;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public boolean isValidRepairItem(ItemStack item, ItemStack repairItem) {
        return repairItem.getItem() == ModItems.METEORIC_INGOT.get();
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
        JojoModConfig.Common config = JojoModConfig.getCommonConfigInstance(isClientSide);
        ForgeConfigSpec.IntValue configOption = higherDurability ? config.arrowDurabilityBeetle : config.arrowDurability;
        return configOption.get();
    }
}
