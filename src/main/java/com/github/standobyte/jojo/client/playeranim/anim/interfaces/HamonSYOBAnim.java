package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.world.entity.player.Player;

public interface HamonSYOBAnim {
    
    public boolean setStartingAnim(Player player);
    public boolean setFinisherAnim(Player player);
    public void stopAnim(Player player);
    
    public static class NoPlayerAnimator implements HamonSYOBAnim {

        @Override
        public boolean setStartingAnim(Player player) {
            return false;
        }

        @Override
        public boolean setFinisherAnim(Player player) {
            return false;
        }

        @Override
        public void stopAnim(Player player) {}
    }
}
