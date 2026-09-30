package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class CrazyDiamondRepairItem extends StandEntityAction {
    public static final StandPose ITEM_FIX_POSE = new StandPose("itemFix");
    private final StandRelativeOffset userOffsetLeftArm;

    public CrazyDiamondRepairItem(StandEntityAction.Builder builder) {
        super(builder);
        this.userOffset.canInvertSide = false;
        this.userOffsetLeftArm = this.userOffset.copyScale(-1, 1, 1);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack itemToRepair = itemToRepair(user);
        if (itemToRepair == null || itemToRepair.isEmpty()) {
            return conditionMessage("item_offhand");
        }
        if (!canBeRepaired(itemToRepair)) {
            return conditionMessage("no_repair");
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public void standTickPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = userPower.getUser();
        if (user != null) {
            if (!world.isClientSide()) {
                ItemStack itemToRepair = itemToRepair(user);
                if (!itemToRepair.isEmpty()) {
                    float points = repairTick(user, standEntity, itemToRepair, task.getTick());
                    if (points > 0) {
                        userPower.addLearningProgressPoints(this, points);
                    }
                }
            }
            else if (ClientUtil.canSeeStands()) {
                CustomParticlesHelper.createCDRestorationParticle(user, InteractionHand.OFF_HAND);
            }
        }
    }

    @Override
    public void appendWarnings(List<Component> list, IStandPower power, Player clientPlayerUser) {
        ItemStack itemToRepair = itemToRepair(clientPlayerUser);
        if (!itemToRepair.isEmpty() && itemToRepair.isEnchanted()) {
            list.add(Component.translatable("jojo.crazy_diamond_fix.warning", itemToRepair.getDisplayName()));
        }
    }
    
    private ItemStack itemToRepair(LivingEntity entity) {
        return entity.getOffhandItem();
    }
    
    public float repairTick(LivingEntity user, StandEntity standEntity, ItemStack itemStack, int taskTicks) {
        int damage = 0;

        ItemStack newStack = null;
        if (itemStack.getItem() == Items.CHIPPED_ANVIL) { 
            newStack = new ItemStack(Items.ANVIL);
            damage = 125;
        }
        else if (itemStack.getItem() == Items.DAMAGED_ANVIL) {
            newStack = new ItemStack(Items.CHIPPED_ANVIL);
            damage = 125;
        }
        else if (itemStack.getItem() == Items.COBBLESTONE) {
            damage = 1;
            newStack = new ItemStack(Items.STONE);
        }
        else if (itemStack.getItem().getRegistryName().getPath().contains("cracked")) {
            ResourceLocation uncracked = new ResourceLocation(
                    itemStack.getItem().getRegistryName().getNamespace(), 
                    itemStack.getItem().getRegistryName().getPath().replace("cracked_", ""));
            if (ForgeRegistries.ITEMS.containsKey(uncracked)) {
                damage = 1;
                newStack = new ItemStack(ForgeRegistries.ITEMS.getValue(uncracked));
            }
        }
        
        if (newStack != null && user instanceof Player) {
            if (itemTransformationTick(taskTicks, standEntity)) {
                Player player = (Player) user;
                user.setItemInHand(InteractionHand.OFF_HAND, ItemUtils.createFilledResult(itemStack, player, newStack, false));
                if (player.abilities.instabuild) {
                    itemStack.shrink(1);
                }
            }
            else {
                damage = -1;
            }
        }
        
        float multiplier = 1;
        if (!itemStack.isEmpty()) {
            switch (itemStack.getRarity()) {
            case UNCOMMON:
                multiplier += 0.5f;
                break;
            case RARE:
                multiplier += 1.5f;
                break;
            case EPIC:
                multiplier += 3;
                break;
            default:
                break;
            }
            if (itemStack.getItem() instanceof TieredItem) {
                int level = ((TieredItem) itemStack.getItem()).getTier().getLevel();
                multiplier += (float) level / 2;
            }
            
            dropExperience(user, itemStack);
            itemStack.removeTagKey("Enchantments");
            itemStack.removeTagKey("StoredEnchantments");
            int damageToRestore = Math.min(itemStack.getDamageValue(), (int) (CrazyDiamondHeal.crazyDRestorationSpeed(standEntity) * 40));
            damage += damageToRestore;
            if (itemStack.isDamageableItem()) {
                itemStack.setDamageValue(itemStack.getDamageValue() - damageToRestore);
                itemStack.setRepairCost(0);
            }
        }
        
        return (float) damage * multiplier * RATE;
    }
    private static final float RATE = 0.002f / 13f;
    
    @Override
    public float resolveLearningMultiplier(IStandPower power) {
        return 1;
    }
    
    public static boolean itemTransformationTick(int taskTicks, StandEntity standEntity) {
        int ticks = (int) (10 / CrazyDiamondHeal.crazyDRestorationSpeed(standEntity));
        return taskTicks % ticks == ticks - 1;
    }
    
    private boolean canBeRepaired(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && 
                (itemStack.isDamaged() || itemStack.isEnchanted()
                        || itemStack.getItem() == Items.CHIPPED_ANVIL || itemStack.getItem() == Items.DAMAGED_ANVIL
                        || itemStack.getItem() == Items.COBBLESTONE
                        || itemStack.getItem().getRegistryName().getPath().contains("cracked") && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(
                                itemStack.getItem().getRegistryName().getNamespace(), 
                                itemStack.getItem().getRegistryName().getPath().replace("cracked_", "")))
                        );
    }
    
    public static void dropExperience(LivingEntity entity, ItemStack enchantedItem) {
        if (!entity.level.isClientSide() && (enchantedItem.hasFoil() || enchantedItem.isEnchanted())) {
            int xp = getExperienceAmount(entity.level, enchantedItem);
    
            if (xp > 0) {
                Vec3 pos = entity.position().add(new Vec3(
                        entity.getBbWidth() * 0.6 * (entity.getMainArm() == HumanoidArm.LEFT ? -1 : 1), 
                        entity.getBbHeight() * (entity.isShiftKeyDown() ? 0.25 : 0.45), 
                        entity.getBbWidth() * 0.7)
                        .yRot(-entity.yBodyRot * MathUtil.DEG_TO_RAD));
                while (xp > 0) {
                    int xpThisOrb = ExperienceOrb.getExperienceValue(xp);
                    xp -= xpThisOrb;
                    entity.level.addFreshEntity(new ExperienceOrb(entity.level, pos.x, pos.y, pos.z, xpThisOrb));
                }
            }
        }
    }

    private static int getExperienceAmount(Level world, ItemStack item) {
        int xp = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(item);

        for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            if (!enchantment.isCurse()) {
                xp += enchantment.getMinCost(level);
            }
        }
        if (xp > 0) {
            int i1 = (int) Math.ceil((double)xp / 2.0D);
            return i1 + world.random.nextInt(i1);
        } else {
            return 0;
        }
    }
    
    @Override
    public void onMaxTraining(IStandPower power) {
        power.unlockAction((StandAction) getShiftVariationIfPresent());
    }
    
    @Override
    public void phaseTransition(Level world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
            else if (from == Phase.PERFORM) {
                standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        if (!standEntity.isArmsOnlyMode()) {
            LivingEntity user = standEntity.getUser();
            if (user.getMainArm() == HumanoidArm.LEFT) {
                return userOffsetLeftArm;
            }
        }
        return super.getOffsetFromUser(standPower, standEntity, task);
    }

    @Override
    public float yRotForOffset(LivingEntity user, StandEntityTask task) {
        return user.yBodyRot;
    }
    
    @Override
    public void rotateStand(StandEntity standEntity, StandEntityTask task) {
        if (standEntity.isArmsOnlyMode()) {
            super.rotateStand(standEntity, task);
        }
        else if (!standEntity.isRemotePositionFixed()) {
            LivingEntity user = standEntity.getUser();
            if (user != null) {
                float rotationOffset = user.getMainArm() == HumanoidArm.RIGHT ? 15 : -15;
                standEntity.setRot(user.yBodyRot + rotationOffset, user.xRot);
                standEntity.setYHeadRot(user.yBodyRot + rotationOffset);
            }
        }
    }
}
