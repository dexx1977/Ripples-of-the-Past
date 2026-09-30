package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.PlayerArmBarrageSwing;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.TwoHandedBarrageAnimation;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IMutableModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;
import com.mojang.math.Axis;

public class KosmXPlayerBarrageAfterimagesAnim extends TwoHandedBarrageAnimation<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> implements IPlayerBarrageAnimation {
    private final BarrageFistAfterimagesLayer modelLayer;
    private final KosmXPlayerBarrageAnim anim;

    public KosmXPlayerBarrageAfterimagesAnim(PlayerModel<AbstractClientPlayer> model, 
            KosmXPlayerBarrageAnim barrageSwing, BarrageFistAfterimagesLayer modelLayer) {
        super(model, barrageSwing, null);
        this.modelLayer = modelLayer;
        this.anim = barrageSwing;
    }

    @Override
    public void animate(Phase phase, float phaseCompletion, AbstractClientPlayer entity, float ticks, 
            float yRotOffsetRad, float xRotRad, HumanoidArm side) {
        if (phase != Phase.PERFORM) {
            super.animate(phase, phaseCompletion, entity, ticks, yRotOffsetRad, xRotRad, side);
        }
    }

    @Override
    public BarrageSwingsHolder<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> getBarrageSwingsHolder(AbstractClientPlayer entity) {
        return BarrageFistAfterimagesLayer.getSwings(entity);
    }

    @Override
    protected float swingsToAdd(AbstractClientPlayer entity, float loop, float lastLoop) {
        return 3 * Math.min(loop - lastLoop, 1) * getLoopLen();
    }
    
    protected float getLoopLen() {
        return super.getLoopLen();
    }

    @Override
    protected double maxSwingOffset(AbstractClientPlayer entity) {
        return 0.625;
    }

    @Override
    protected void addSwing(AbstractClientPlayer entity, BarrageSwingsHolder<AbstractClientPlayer, 
            PlayerModel<AbstractClientPlayer>> swings, HumanoidArm side, float f, double maxOffset) {
        swings.addSwing(new PlayerArmBarrageSwing(this, f, getLoopLen(), side, maxOffset, modelLayer));
    }

    @Override
    public void beforeSwingsRender(PoseStack matrixStack, BarrageFistAfterimagesLayer playerModelLayer) {
        PlayerModel<AbstractClientPlayer> model = playerModelLayer.getParentModel();
        if (model instanceof IMutableModel) {
            AnimationProcessor anim = ((IMutableModel) model).getEmoteSupplier().get();
            if (anim != null) {
                float yRot = anim.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO).getY();
                matrixStack.mulPose(Axis.YP.rotation(yRot));
            }
        }
    }
    
    @Override
    public void beforeSwingAfterimageRender(PoseStack matrixStack, 
            PlayerModel<AbstractClientPlayer> model, float loopCompletion, HumanoidArm side) {
        anim.rotateBody(matrixStack, loopCompletion, side);
    }
}
