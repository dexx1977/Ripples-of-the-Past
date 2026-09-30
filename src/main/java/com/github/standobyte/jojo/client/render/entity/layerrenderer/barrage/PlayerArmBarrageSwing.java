package com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage;

import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.ArmBarrageSwing;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.IBarrageAnimation;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;

@Deprecated
public class PlayerArmBarrageSwing extends ArmBarrageSwing<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final BarrageFistAfterimagesLayer effectLayer;

    public PlayerArmBarrageSwing(IBarrageAnimation<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> barrageAnim, 
            float ticks, float ticksMax, HumanoidArm side, double maxOffset, 
            BarrageFistAfterimagesLayer effectLayer) {
        super(barrageAnim, ticks, ticksMax, side, maxOffset);
        this.effectLayer = effectLayer;
    }

    @Override
    protected void setArmOnlyModelVisibility(AbstractClientPlayer entity, PlayerModel<AbstractClientPlayer> model, HumanoidArm side) {
        effectLayer.setArmsVisibility(model, side);
    }

}
