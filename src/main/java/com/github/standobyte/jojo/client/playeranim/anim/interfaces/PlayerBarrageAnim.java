package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;

public interface PlayerBarrageAnim extends BasicToggleAnim {
    
    IPlayerBarrageAnimation createBarrageAfterimagesAnim(PlayerModel<AbstractClientPlayer> model, BarrageFistAfterimagesLayer layer);
    
    
    
    public static class NoPlayerAnimator extends BasicToggleAnim.NoPlayerAnimator implements PlayerBarrageAnim {
        
        @Override
        public IPlayerBarrageAnimation createBarrageAfterimagesAnim(PlayerModel<AbstractClientPlayer> model,
                BarrageFistAfterimagesLayer layer) {
            return null;
        }
        
    }
}
