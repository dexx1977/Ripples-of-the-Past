package com.github.standobyte.jojo.client.playeranim.kosmx.anim.mob;

import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXBendyLibHelper;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import net.minecraft.client.model.geom.ModelPart;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.IMutableModel;
import net.minecraft.core.Direction;

public class KosmXHamonMasterAnimApplier extends KosmXEntityAnimApplier<HamonMasterEntity, HamonMasterModel> {
    private final AnimationProcessor animProcessor;
    
    private final ModelPart mutatedJacket;
    private final ModelPart mutatedRightSleeve;
    private final ModelPart mutatedLeftSleeve;
    private final ModelPart mutatedRightPantLeg;
    private final ModelPart mutatedLeftPantLeg;
    
    public KosmXHamonMasterAnimApplier(HamonMasterModel model, IMutableModel modelWithMixin, IAnimation sittingAnim) {
        super(model, modelWithMixin);
        this.animProcessor = new AnimationProcessor(sittingAnim);
        
        // the model's own clothes and limbs are the bendable parts; the directions
        // are the ones the old addBendedCuboid calls used
        mutatedJacket = model.jacket;
        mutatedRightSleeve = model.rightSleeve;
        mutatedLeftSleeve = model.leftSleeve;
        mutatedRightPantLeg = model.rightPants;
        mutatedLeftPantLeg = model.leftPants;
        KosmXBendyLibHelper.initBend(mutatedJacket, Direction.DOWN);
        KosmXBendyLibHelper.initBend(mutatedLeftSleeve, Direction.UP);
        KosmXBendyLibHelper.initBend(mutatedRightSleeve, Direction.UP);
        KosmXBendyLibHelper.initBend(mutatedLeftPantLeg, Direction.UP);
        KosmXBendyLibHelper.initBend(mutatedRightPantLeg, Direction.UP);
        // playerAnimator prepares the vanilla player model's parts itself; these are
        // the mod's own model's limbs
        KosmXBendyLibHelper.initBend(model.leftArm, Direction.UP);
        KosmXBendyLibHelper.initBend(model.leftLeg, Direction.UP);
    }
    
    @Override
    public void onInit() {
        SetableSupplier<AnimationProcessor> animProcessor = modelWithMixin.getEmoteSupplier();
        animProcessor.set(this.animProcessor);
        modelWithMixin.setEmoteSupplier(animProcessor);
    }
    
    @Override
    public void setEmote() {
        super.setEmote();
        
        // the cloth bends with the limb it hangs from
        KosmXBendyLibHelper.copyBend(model.body, mutatedJacket);
        KosmXBendyLibHelper.copyBend(model.leftLeg, mutatedLeftPantLeg);
        KosmXBendyLibHelper.copyBend(model.rightLeg, mutatedRightPantLeg);
        KosmXBendyLibHelper.copyBend(model.leftArm, mutatedLeftSleeve);
        KosmXBendyLibHelper.copyBend(model.rightArm, mutatedRightSleeve);
    }
}
