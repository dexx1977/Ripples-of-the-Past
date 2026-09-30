package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.world.entity.player.Player;

public interface BasicToggleAnim {
    
    boolean setAnimEnabled(Player player, boolean enabled);
    
    public static class NoPlayerAnimator implements BasicToggleAnim {
        public static final BasicToggleAnim DUMMY = new NoPlayerAnimator();

        @Override
        public boolean setAnimEnabled(Player player, boolean enabled) { return false; }
        
    }
    
}
