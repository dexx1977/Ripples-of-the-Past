package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

public class KosmXRebuffOverdriveHandler extends KosmXWindupAttackHandler {

    public KosmXRebuffOverdriveHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayer player) {
        return new ModifierLayer<>(null);
    }
    
    
    private static final ResourceLocation REBUFF_OVERDRIVE = new ResourceLocation(JojoMod.MOD_ID, "rebuff_overdrive");
    
    @Override
    public boolean setWindupAnim(Player player) {
        return setAnimFromName(player, REBUFF_OVERDRIVE);
    }

    @Override
    public boolean setAttackAnim(Player player) {
        return setToSwingTick(player, 0, REBUFF_OVERDRIVE);
    }
    
    @Override
    public void stopAnim(Player player) {
        fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }
    
}
