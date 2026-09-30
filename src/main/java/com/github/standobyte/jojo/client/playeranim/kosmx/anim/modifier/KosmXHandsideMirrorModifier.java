package com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;

public class KosmXHandsideMirrorModifier extends KosmXFixedMirrorModifier {
    private final AbstractClientPlayer player;
    
    public KosmXHandsideMirrorModifier(AbstractClientPlayer player) {
        this.player = player;
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            return anim == null ? value0 : anim.get3DTransform(modelName, type, tickDelta, value0);
        }
        return super.get3DTransform(modelName, type, tickDelta, value0);
    }
    
}
