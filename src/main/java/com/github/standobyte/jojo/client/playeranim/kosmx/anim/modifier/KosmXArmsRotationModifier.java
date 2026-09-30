package com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier;

import java.util.EnumSet;
import java.util.Set;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.general.MathUtil;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;

public class KosmXArmsRotationModifier extends AbstractModifier {
    private final AbstractClientPlayer entity;
    private final Set<HumanoidArm> arms = EnumSet.noneOf(HumanoidArm.class);
    
    public KosmXArmsRotationModifier(AbstractClientPlayer entity, HumanoidArm side, HumanoidArm... otherSide) {
        this.entity = entity;
        arms.add(side);
        for (HumanoidArm s : otherSide) {
            arms.add(s);
        }
    }

    @Override
    public Vec3f get3DTransform(String modelName, ItemDisplayContext type, float tickDelta, Vec3f value0) {
        Vec3f transform = super.get3DTransform(modelName, type, tickDelta, value0);
        if (isActive() && type == TransformType.ROTATION && (
                arms.contains(HumanoidArm.LEFT) && "leftArm".equals(modelName)
                || arms.contains(HumanoidArm.RIGHT) && "rightArm".equals(modelName))) {
            float entityXRot = entity.xRot;
            Vector3f anglesNew = ClientUtil.rotateAngles(transform.getX(), transform.getY(), transform.getZ(), entityXRot * MathUtil.DEG_TO_RAD);
            transform = new Vec3f(anglesNew.x(), anglesNew.y(), anglesNew.z());
        }
        return transform;
    }
}
