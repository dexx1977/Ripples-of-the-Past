package com.github.standobyte.jojo.capability.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraftforge.common.util.INBTSerializable;

public class MerchantData implements INBTSerializable<CompoundTag> {
    private final LivingEntity entity;
    private final Merchant asMerchant;
    private final Collection<UUID> refuseTradingWith = new HashSet<>();
    
    private final Multimap<UUID, String> playersTriedTrading = ArrayListMultimap.create();
    
    public MerchantData(LivingEntity entity, Merchant asMerchant) {
        this.entity = entity;
        this.asMerchant = asMerchant;
    }
    
    public boolean gaveUniqueTrade() {
        return entity.getTags().contains("JojoUniqueTrade");
    }
    
    public void setGaveUniqueTrade() {
        entity.addTag("JojoUniqueTrade");
    }
    
    public void setPlayerTriedTrading(Player player, String tradeType) {
        playersTriedTrading.put(player.getUUID(), tradeType);
    }
    
    public boolean getPlayerTriedTrading(Player player, String tradeType) {
        return playersTriedTrading.containsEntry(player.getUUID(), tradeType);
    }
    
    public void resetPlayerTrades(Player player) {
        playersTriedTrading.removeAll(player.getUUID());
    }
    
    public boolean resetPlayerTrade(Player player, String tradeType) {
        return playersTriedTrading.remove(player.getUUID(), tradeType);
    }
    
    public void setRefuseTrading(UUID playerUUID, boolean refuse) {
        if (refuse) {
            refuseTradingWith.add(playerUUID);
        }
        else {
            refuseTradingWith.remove(playerUUID);
        }
    }
    
    public boolean refusesTradingWith(Player player) {
        return refuseTradingWith.contains(player.getUUID());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        
        CompoundTag playerTriesNbt = new CompoundTag();
        playersTriedTrading.asMap().forEach((id, tradeTypes) -> {
            if (!tradeTypes.isEmpty()) {
                ListTag typesListNbt = new ListTag();
                tradeTypes.forEach(tradeType -> typesListNbt.add(StringTag.valueOf(tradeType)));
                playerTriesNbt.put(id.toString(), typesListNbt);
            }
        });
        nbt.put("PlayerTries", playerTriesNbt);
        
        if (!refuseTradingWith.isEmpty()) {
            ListTag refuseTradingWithNbt = new ListTag();
            for (UUID id : this.refuseTradingWith) {
                refuseTradingWithNbt.add(NbtUtils.createUUID(id));
            }
            nbt.put("RefuseTrade", refuseTradingWithNbt);
        }
        
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        MCUtil.nbtGetCompoundOptional(nbt, "PlayerTries").ifPresent(playerTriesNbt -> {
            playerTriesNbt.getAllKeys().forEach(key -> {
                try {
                    UUID playerUuid = UUID.fromString(key);
                    playerTriesNbt.getList(key, Tag.TAG_STRING).forEach(tradeType -> {
                        playersTriedTrading.put(playerUuid, ((StringTag) tradeType).getAsString());
                    });
                }
                catch (Exception e) {
                    JojoMod.getLogger().error(e);
                }
            });
        });
        
        refuseTradingWith.clear();
        MCUtil.getNbtElement(nbt, "RefuseTrade", ListTag.class).ifPresent(refuseTradingWithNbt -> {
            for (Tag element : refuseTradingWithNbt) {
                try {
                    UUID id = NbtUtils.loadUUID(element);
                    this.refuseTradingWith.add(id);
                }
                catch (IllegalArgumentException e) {
                    break;
                }
            }
        });
    }
}
