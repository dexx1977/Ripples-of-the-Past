package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TrBarrageHitSoundPacket {
    private final int standEntityId;
    private final boolean hit;
    private final SoundEvent sound;
    private final Vec3 soundPos;
    
    public TrBarrageHitSoundPacket(int standEntityId, SoundEvent sound, Vec3 soundPos) {
        this(standEntityId, true, sound, soundPos);
    }
    
    public static TrBarrageHitSoundPacket noSound(int standEntityId) {
        return new TrBarrageHitSoundPacket(standEntityId, false, null, null);
    }
    
    private TrBarrageHitSoundPacket(int standEntityId, boolean hit, SoundEvent sound, Vec3 soundPos) {
        this.standEntityId = standEntityId;
        this.hit = hit;
        this.sound = sound;
        this.soundPos = soundPos;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrBarrageHitSoundPacket> {

        @Override
        public void encode(TrBarrageHitSoundPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.standEntityId);
            buf.writeBoolean(msg.hit);
            if (msg.hit) {
                buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
                NetworkUtil.writeVecApproximate(buf, msg.soundPos);
            }
        }

        @Override
        public TrBarrageHitSoundPacket decode(FriendlyByteBuf buf) {
            int entityId = buf.readInt();
            boolean hit = buf.readBoolean();
            if (hit) {
                return new TrBarrageHitSoundPacket(entityId, buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), NetworkUtil.readVecApproximate(buf));
            }
            else {
                return TrBarrageHitSoundPacket.noSound(entityId);
            }
        }

        @Override
        public void handle(TrBarrageHitSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.standEntityId);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) entity;
                if (msg.hit) {
                    stand.getBarrageHitSoundsHandler().hit(msg.sound, msg.soundPos);
                }
                else {
                    stand.getBarrageHitSoundsHandler().hitMissed();
                }
            }
        }

        @Override
        public Class<TrBarrageHitSoundPacket> getPacketClass() {
            return TrBarrageHitSoundPacket.class;
        }
    }
}
