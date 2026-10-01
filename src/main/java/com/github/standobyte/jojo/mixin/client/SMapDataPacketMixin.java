package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mc.CustomTargetIconMap;
import com.github.standobyte.jojo.util.mc.CustomTargetIconMap.IMapDataMixin;

import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(ClientboundMapItemDataPacket.class)
public class SMapDataPacketMixin {
    // 1.16.5 used a mutable MapDecoration[] shadowed by this mixin; 1.20.1 stores
    // an immutable List in the packet, so the icons are replaced on the saved data
    // (which keeps the mutable Map exposed via MapDataMixin/IMapDataMixin) after
    // the packet has been applied.
    @Inject(method = "applyToMap", at = @At("RETURN"))
    public void jojoReplaceMapDecorations(MapItemSavedData mapdata, CallbackInfo ci) {
        if (mapdata instanceof IMapDataMixin) {
            CustomTargetIconMap.CustomIconMapDecoration.replaceWithCustomIcons(((IMapDataMixin) mapdata).decorations());
        }
    }
}
