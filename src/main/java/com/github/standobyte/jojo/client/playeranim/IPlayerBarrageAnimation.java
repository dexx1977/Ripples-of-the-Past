package com.github.standobyte.jojo.client.playeranim;

import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.IBarrageAnimation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;

public interface IPlayerBarrageAnimation extends IBarrageAnimation<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    void beforeSwingsRender(PoseStack matrixStack, BarrageFistAfterimagesLayer playerModelLayer);
}
