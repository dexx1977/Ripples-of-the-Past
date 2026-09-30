package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfBindingEntity;

import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

// Made with Blockbench 3.9.2


public class SatiporojaScarfBindingModel extends RepeatingModel<SatiporojaScarfBindingEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart scarf;
    private final ModelPart scarfExtending;

    public SatiporojaScarfBindingModel() {
        texWidth = 32;
        texHeight = 32;

        scarf = new ModelPart(this);
        scarf.setPos(0.0F, 0.0F, 0.0F);
        scarf.texOffs(0, 0).addBox(-4.0F, -2.5F, -0.5F, 8.0F, 2.0F, 8.0F, 0.2F, false);

        scarfExtending = new ModelPart(this);
        scarfExtending.setPos(0.0F, 0.0F, 0.0F);
        scarfExtending.texOffs(0, 10).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 12.0F, -0.3F, false);
    }

    @Override
    protected ModelPart getMainPart() {
        return scarf;
    }
    
    @Override
    protected float getMainPartLength() {
        return 8.4F;
    }
    
    @Override
    protected ModelPart getRepeatingPart() {
        return scarfExtending;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 11.4F;
    }
}
