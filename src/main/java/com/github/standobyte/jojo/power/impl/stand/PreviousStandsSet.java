package com.github.standobyte.jojo.power.impl.stand;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PreviousStandTypesPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

public class PreviousStandsSet {
    private final Set<StandType<?>> stands = new HashSet<>();
    
    
    public void addStand(StandType<?> standType, LivingEntity user) {
        if (stands.add(standType)) {
            if (user instanceof ServerPlayer) {
                PacketManager.sendToClient(PreviousStandTypesPacket.newStand(standType), (ServerPlayer) user);
            }
        }
    }
    
    public void clear() {
        stands.clear();
    }
    
    
    public boolean contains(StandType<?> standType) {
        return stands.contains(standType);
    }
    
    public boolean containsAll(Collection<StandType<?>> standsToCheck) {
        return stands.containsAll(standsToCheck);
    }
    
    
    public List<StandType<?>> rigForUnusedStands(List<StandType<?>> availableStands) {
        List<StandType<?>> notUsedStands = availableStands.stream()
                .filter(stand -> !stands.contains(stand))
                .collect(Collectors.toList());
        return !notUsedStands.isEmpty() ? notUsedStands : availableStands;
    }
    
    
    public void syncWithUser(ServerPlayer user) {
        PacketManager.sendToClient(PreviousStandTypesPacket.allStands(stands), user);
    }
    
    public void handlePacket(PreviousStandTypesPacket packet) {
        if (packet.clear) {
            clear();
        }
        else if (packet.sendingAll) {
            if (packet.allStands != null) {
                stands.addAll(packet.allStands);
            }
        }
        else {
            if (packet.newStand != null) {
                stands.add(packet.newStand);
            }
        }
    }
    
    
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        
        ListTag standsListNbt = new ListTag();
        stands.forEach(stand -> {
            standsListNbt.add(StringTag.valueOf(JojoCustomRegistries.STANDS.getKeyAsString(stand)));
        });
        nbt.put("Stands", standsListNbt);
        
        return nbt;
    }
    
    public void fromNBT(CompoundTag nbt) {
        if (nbt.contains("Stands", MCUtil.getNbtId(ListTag.class))) {
            ListTag standsListNbt = nbt.getList("Stands", MCUtil.getNbtId(StringTag.class));
            standsListNbt.forEach(standNameNbt -> {
                String standName = ((StringTag) standNameNbt).getAsString();
                StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standName));
                if (standType != null) {
                    stands.add(standType);
                }
            });
        }
    }
}
