package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanRibEntity;

import net.minecraft.client.model.geom.ModelPart;

// Made with Blockbench 3.9.2


public class PillarmanRibModel extends RepeatingModel<PillarmanRibEntity> {
	private final ModelPart finger;
    private final ModelPart fingerExtending;

    public PillarmanRibModel() {
        texWidth = 32;
        texHeight = 32;

        finger = new ModelPart(this);
        finger.setPos(0.0F, 0.0F, 0.0F);
        finger.texOffs(0, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 8.0F, 0.0F, false);

        fingerExtending = new ModelPart(this);
        fingerExtending.setPos(0.0F, 0.0F, 0.0F);
        fingerExtending.texOffs(0, 0).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 1.0F, 8.0F, 0.0F, false);
    }

    @Override
    protected ModelPart getMainPart() {
        return finger;
    }
    
    @Override
    protected float getMainPartLength() {
        return 2F;
    }
    
    @Override
    protected ModelPart getRepeatingPart() {
        return fingerExtending;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 8F;
    }
}
