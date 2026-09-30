package com.github.standobyte.jojo.power.impl.stand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class StandInstance {
    private final StandType<?> standType;
    private final EnumSet<StandPart> parts = EnumSet.allOf(StandPart.class);
    private Optional<Component> customName = Optional.empty();
    private Optional<ResourceLocation> standSkin = Optional.empty();
    private boolean isDirty;
    
    public StandInstance(@Nonnull StandType<?> standType) {
        this.standType = standType;
    }
    
    public StandType<?> getType() {
        return standType;
    }
    
    public boolean hasPart(StandPart part) {
        return parts.contains(part);
    }
    
    public boolean removePart(StandPart part) {
        boolean removed = parts.remove(part);
        isDirty |= removed;
        return removed;
    }
    
    public boolean addPart(StandPart part) {
        boolean added = parts.add(part);
        isDirty |= added;
        return added;
    }
    
    public EnumSet<StandPart> getAllParts() {
        return parts;
    }
    
    public void setCustomName(Component customName) {
        isDirty |= this.customName.map(name -> !name.equals(customName)).orElse(customName != null);
        this.customName = Optional.ofNullable(customName);
    }
    
    public Component getName() {
        return customName.orElse(standType.getName());
    }
    
    public void setCustomSkin(Optional<ResourceLocation> skinLocation, @Nullable IStandPower power) {
        if (skinLocation.isPresent() && skinLocation.get().equals(standType.getRegistryName())) {
            skinLocation = Optional.empty();
        }
        
        isDirty |= (this.standSkin.isPresent() ^ skinLocation.isPresent())
                || this.standSkin.isPresent() && skinLocation.map(skinNew -> !skinNew.equals(this.standSkin.get())).orElse(false);
        this.standSkin = skinLocation;
        if (power != null) {
            standType.onStandSkinSet(power, skinLocation);
        }
    }
    
    public Optional<ResourceLocation> getSelectedSkin() {
        return standSkin;
    }
    
    public void tick(IStandPower standPower, LivingEntity standUser, Level world) {
        syncIfDirty(standUser);
    }
    
    public void syncIfDirty(LivingEntity standUser) {
        if (isDirty && !standUser.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrTypeStandInstancePacket(standUser.getId(), this, -1), standUser);
        }
        isDirty = false;
    }
    
    

    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        
        nbt.putString("StandType", JojoCustomRegistries.STANDS.getKeyAsString(standType));
        
        CompoundTag missingLimbsNbt = new CompoundTag();
        boolean limbsMissing = false;
        for (StandPart limbs : StandPart.values()) {
            if (!this.parts.contains(limbs)) {
                missingLimbsNbt.putBoolean(limbs.name(), true);
                limbsMissing = true;
            }
        }
        if (limbsMissing) {
            nbt.put("MissingLimbs", missingLimbsNbt);
        }
        
        customName.ifPresent(name -> nbt.putString("CustomName", Component.Serializer.toJson(name)));
        standSkin.ifPresent(skinId -> nbt.putString("Skin", skinId.toString()));
        
        return nbt;
    }

    public static StandInstance fromNBT(CompoundTag nbt) {
        String standName = nbt.getString("StandType");
        StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standName));
        if (standType == null) {
            return null;
        }
        
        StandInstance instance = new StandInstance(standType);
        
        if (nbt.contains("MissingLimbs", MCUtil.getNbtId(CompoundTag.class))) {
            CompoundTag missingLimbsNbt = nbt.getCompound("MissingLimbs");
            for (StandPart limbs : StandPart.values()) {
                if (missingLimbsNbt.getBoolean(limbs.name())) {
                    instance.parts.remove(limbs);
                }
            }
        }

        if (nbt.contains("CustomName", MCUtil.getNbtId(StringTag.class))) {
            String name = nbt.getString("CustomName");
            try {
                instance.setCustomName(Component.Serializer.fromJson(name));
            } catch (Exception exception) {
                JojoMod.getLogger().warn("Failed to parse custom Stand name {}", name, exception);
            }
        }
        
        instance.setCustomSkin(MCUtil.getNbtElement(nbt, "Skin", StringTag.class)
                .map(StringTag::getAsString).map(ResourceLocation::new), 
                null);
        
        return instance;
    }
    
    public void toBuf(FriendlyByteBuf buf) {
        buf.writeRegistryId(standType);
        
        Set<StandPart> missingParts = EnumSet.complementOf(parts);
        buf.writeVarInt(missingParts.size());
        missingParts.forEach(part -> buf.writeEnum(part));
        
        EntityDataSerializers.OPTIONAL_COMPONENT.write(buf, customName);
        NetworkUtil.writeOptional(buf, standSkin, buf::writeResourceLocation);
    }
    
    public static StandInstance fromBuf(FriendlyByteBuf buf) {
        StandType<?> standType = buf.readRegistryIdSafe(StandType.class);
        StandInstance standInstance = new StandInstance(standType);
        
        int missingPartsCount = buf.readVarInt();
        for (int i = 0; i < missingPartsCount; i++) {
            standInstance.parts.remove(buf.readEnum(StandPart.class));
        }
        
        standInstance.customName = EntityDataSerializers.OPTIONAL_COMPONENT.read(buf);
        standInstance.standSkin = NetworkUtil.readOptional(buf, FriendlyByteBuf::readResourceLocation);
        
        return standInstance;
    }
    
    
    
    
    public enum StandPart {
        MAIN_BODY,
        ARMS,
        LEGS
    }

}
