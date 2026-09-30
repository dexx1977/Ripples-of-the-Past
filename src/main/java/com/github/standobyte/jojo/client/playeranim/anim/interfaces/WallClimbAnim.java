package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.world.entity.player.Player;

public interface WallClimbAnim extends BasicToggleAnim {
    
    public void tickAnimProperties(Player player, boolean isMoving, 
            double movementUp, double movementLeft, float speed);
    
    public static class NoPlayerAnimator extends BasicToggleAnim.NoPlayerAnimator implements WallClimbAnim {

        @Override
        public void tickAnimProperties(Player player, boolean isMoving, 
                double movementUp, double movementLeft, float speed) {}
    }
}
