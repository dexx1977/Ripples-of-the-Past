package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;
import com.github.standobyte.jojo.util.mc.MCUtil;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

public class KosmXSendoWaveKickHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements BasicToggleAnim {

    public KosmXSendoWaveKickHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayer player) {
        return new ModifierLayer<>(null, new KosmXHeadRotationModifier());
    }
    
    
    @Override
    public boolean setAnimEnabled(Player player, boolean enabled) {
        enabled &= PlayerAnimationHandler.canAnimate(player);
        if (enabled) {
            return fadeOutAnim((AbstractClientPlayer) player, KosmXFixedFadeModifier.standardFadeIn(4, Ease.OUTCUBIC), 
                    getAnimFromName(getAnimPath(player)));
        }
        else {
            return fadeOutAnim((AbstractClientPlayer) player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
        }
    }
    
    private static final ResourceLocation NO_ITEMS = new ResourceLocation(JojoMod.MOD_ID, "sendo_wave_kick");
    private static final ResourceLocation ITEM_LEFT = new ResourceLocation(JojoMod.MOD_ID, "sendo_wave_kick_l");
    private static final ResourceLocation ITEM_RIGHT = new ResourceLocation(JojoMod.MOD_ID, "sendo_wave_kick_r");
    private static final ResourceLocation ITEMS_BOTH = new ResourceLocation(JojoMod.MOD_ID, "sendo_wave_kick_lr");
    private static final ResourceLocation[] ANIMS = new ResourceLocation[] {
            NO_ITEMS,
            ITEM_LEFT,
            ITEM_RIGHT,
            ITEMS_BOTH
    };
    private ResourceLocation getAnimPath(Player player) {
        InteractionHand rightHand = MCUtil.getHand(player, HumanoidArm.RIGHT);
        InteractionHand leftHand = MCUtil.getHand(player, HumanoidArm.LEFT);
        int index = (player.getItemInHand(leftHand).isEmpty() ? 0 : 1) + (player.getItemInHand(rightHand).isEmpty() ? 0 : 2);
        return ANIMS[index];
    }
    
}
