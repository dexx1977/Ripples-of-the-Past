package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.world.entity.player.Player;

public interface WindupAttackAnim {
    
    public boolean setWindupAnim(Player player);
    public boolean setAttackAnim(Player player);
    public void stopAnim(Player player);
    
    public static class NoPlayerAnimator implements WindupAttackAnim {

        @Override
        public boolean setWindupAnim(Player player) {
            return false;
        }

        @Override
        public boolean setAttackAnim(Player player) {
            return false;
        }

        @Override
        public void stopAnim(Player player) {}
    }
}
