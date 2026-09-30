package com.github.standobyte.jojo.power.impl.stand;

import java.util.UUID;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.JojoModConfig.Common;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.enchantment.StandArrowXpReductionEnchantment;
import com.github.standobyte.jojo.init.ModEnchantments;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ArrowXpLevelsDataPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Constants;
import net.minecraft.nbt.Tag;

public class StandArrowHandler {
    private int xpLevelsTakenByArrow;
    private int standsGotFromArrow;
    private UUID standArrowShooterUUID;
    private ItemStack standArrowItem = ItemStack.EMPTY;
    private boolean healStandArrowDamage;
    private StandType<?> standToGive;
    
    
    
    public void startArrowEffectSetStand(StandType<?> standType) {
        this.standToGive = standType;
    }
    
    public void tick(LivingEntity user) {
        if (healStandArrowDamage) {
            if (user.hasEffect(ModStatusEffects.BLEEDING.get())) {
                user.removeEffect(ModStatusEffects.BLEEDING.get());
            }
            if (user.getHealth() < user.getMaxHealth()) {
                user.heal(0.25F);
            }
            else {
                healStandArrowDamage = false;
            }
        }
    }
    
    public void clear() {
        this.standsGotFromArrow = 0;
    }
    
    public void keepOnDeath(StandArrowHandler handler) {
        this.standsGotFromArrow = handler.standsGotFromArrow;
    }
    
    public void syncWithUser(ServerPlayer user) {
        PacketManager.sendToClient(new ArrowXpLevelsDataPacket(xpLevelsTakenByArrow, standsGotFromArrow), user);
    }
    
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("ArrowLevels", xpLevelsTakenByArrow);
        nbt.putInt("ArrowStands", standsGotFromArrow);
        nbt.put("ArrowItem", standArrowItem.save(new CompoundTag()));
        if (standToGive != null) {
            nbt.putString("StandToGive", standToGive.getRegistryName().toString());
        }
        return nbt;
    }
    
    public void fromNBT(CompoundTag nbt) {
        xpLevelsTakenByArrow = nbt.getInt("ArrowLevels");
        standsGotFromArrow = nbt.getInt("ArrowStands");
        if (nbt.contains("ArrowItem", MCUtil.getNbtId(CompoundTag.class))) {
            standArrowItem = ItemStack.of(nbt.getCompound("ArrowItem"));
        }
        if (nbt.contains("StandToGive", Tag.TAG_STRING)) {
            ResourceLocation id = new ResourceLocation(nbt.getString("StandToGive"));
            standToGive = JojoCustomRegistries.STANDS.getValue(id);
        }
    }
    
    
    
    public int decXpLevelsTakenByArrow(LivingEntity user) {
        setXpLevelsTakenByArrow(this.xpLevelsTakenByArrow + 1, user);
        return this.xpLevelsTakenByArrow;
    }
    
    public void setXpLevelsTakenByArrow(int levels, LivingEntity user) {
        this.xpLevelsTakenByArrow = levels;
        if (user instanceof ServerPlayer) {
            PacketManager.sendToClient(new ArrowXpLevelsDataPacket(xpLevelsTakenByArrow, standsGotFromArrow), (ServerPlayer) user);
        }
    }
    
    public int getXpLevelsTakenByArrow() {
        return xpLevelsTakenByArrow;
    }
    
    public int getStandXpLevelsRequirement(boolean clientSide, ItemStack arrowItem) {
        Common config = JojoModConfig.getCommonConfigInstance(clientSide);
        int levels = config.standXpCostInitial.get() + standsGotFromArrow * config.standXpCostIncrease.get();
        levels = Math.max(levels - StandArrowXpReductionEnchantment.getXpRequirementReduction(
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STAND_ARROW_XP_REDUCTION.get(), arrowItem)), 0);
        return levels;
    }
    
    public StandType<?> getStandToGive() {
        return standToGive;
    }
    
    public void clearStandToGive() {
        standToGive = null;
    }
    
    public void onGettingStandFromArrow(LivingEntity user) {
        xpLevelsTakenByArrow = 0;
        standsGotFromArrow++;
        if (!user.level.isClientSide()) {
            if (user instanceof ServerPlayer) {
                PacketManager.sendToClient(new ArrowXpLevelsDataPacket(xpLevelsTakenByArrow, standsGotFromArrow), (ServerPlayer) user);
            }

            if (standArrowShooterUUID != null) {
                Player shooter = ((ServerLevel) user.level).getPlayerByUUID(standArrowShooterUUID);
                if (shooter != null) {
                    ModCriteriaTriggers.STAND_ARROW_HIT.get().trigger((ServerPlayer) shooter, user, true);
                }
                standArrowShooterUUID = null;
            }
            
            healStandArrowDamage = true;
        }
    }
    
    public void setStandArrowShooter(Entity shooter) {
        this.standArrowShooterUUID = shooter.getUUID();
    }
    
    public void setStandArrowItem(ItemStack item) {
        if (item != null) {
            this.standArrowItem = item.copy();
        }
    }
    
    public ItemStack getStandArrowItem() {
        return standArrowItem;
    }
    
    public void setFromPacket(ArrowXpLevelsDataPacket packet) {
        this.xpLevelsTakenByArrow = packet.levels;
        this.standsGotFromArrow = packet.gotStands;
    }

}
