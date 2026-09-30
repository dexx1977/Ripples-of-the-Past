package com.github.standobyte.jojo.client.playeranim.kosmx;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler.BendablePart;

import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.bendylib.impl.ICuboid;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

/**
 * Bridges the mod's bending needs to the 1.20.1 playerAnimator API.
 *
 * <p>1.16.5's API handed out a bendable wrapper per model part, which also
 * remembered the current bend ({@code IBendHelper.create(...)},
 * {@code IMutableModel.getLeftArm()}). The 1.20.1 API is stateless: the part itself
 * is passed to {@code IBendHelper.bend(part, bendX, bendY)}. PlayerAnimator's own
 * mixins prepare a biped model's body facing down and its limbs facing up, which is
 * also the direction the mod needs for its own models, so parts are prepared with
 * those directions on first use and the current bend is read back from the bendable
 * cuboid the library attaches to the part.</p>
 */
public class KosmXBendyLibHelper {
    private static final Set<ModelPart> PREPARED = Collections.newSetFromMap(new WeakHashMap<>());

    public static Direction bendDirection(BendablePart part) {
        return part == BendablePart.TORSO ? Direction.DOWN : Direction.UP;
    }

    /** The model part a bendable part refers to. */
    public static ModelPart getPart(HumanoidModel<?> model, BendablePart part) {
        switch (part) {
        case TORSO:
            return model.body;
        case LEFT_ARM:
            return model.leftArm;
        case RIGHT_ARM:
            return model.rightArm;
        case LEFT_LEG:
            return model.leftLeg;
        case RIGHT_LEG:
            return model.rightLeg;
        }
        return null;
    }

    public static void bend(HumanoidModel<?> model, BendablePart part, Pair<Float, Float> bend) {
        bend(model, part, bend.getLeft(), bend.getRight());
    }

    public static void bend(HumanoidModel<?> model, BendablePart part, float bendX, float bendY) {
        bend(getPart(model, part), bendDirection(part), bendX, bendY);
    }

    /** Applies a bend, preparing the part the first time it is used. */
    public static void bend(ModelPart part, Direction direction, float bendX, float bendY) {
        if (part == null) {
            return;
        }
        if (PREPARED.add(part)) {
            IBendHelper.INSTANCE.initBend(part, direction);
        }
        IBendHelper.INSTANCE.bend(part, bendX, bendY);
    }

    /** The current bend of a part as {@code {bendX, bendY}}; zeroes if it is not bendable. */
    public static float[] getBend(ModelPart part) {
        if (part == null) {
            return new float[] {0, 0};
        }
        return ModelPartAccessor.optionalGetCuboid(part, 0)
                .map(cuboid -> {
                    BendableCuboid bendable = bendableOf(cuboid);
                    return bendable != null ? new float[] {bendable.getBendX(), bendable.getBendY()} : new float[] {0, 0};
                })
                .orElse(new float[] {0, 0});
    }

    public static float[] getBend(HumanoidModel<?> model, BendablePart part) {
        return getBend(getPart(model, part));
    }

    private static BendableCuboid bendableOf(MutableCuboid cuboid) {
        ICuboid active = cuboid.getActiveMutator() != null ? cuboid.getActiveMutator().getB() : cuboid.getMutator("bend");
        return active instanceof BendableCuboid ? (BendableCuboid) active : null;
    }
}
