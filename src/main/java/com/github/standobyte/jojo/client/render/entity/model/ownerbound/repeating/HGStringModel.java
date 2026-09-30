package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;

import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

// Made with Blockbench 3.9.2


public class HGStringModel<T extends OwnerBoundProjectileEntity> extends RepeatingModel<T> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart barrier;

    public HGStringModel() {
        texWidth = 32;
        texHeight = 32;

        barrier = new ModelPart(this);
        barrier.setPos(0.0F, 0.0F, 0.0F);
        barrier.texOffs(0, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 8.0F, 0.0F, false);
    }

    @Override
    protected ModelPart getMainPart() {
        return null;
    }
    
    @Override
    protected float getMainPartLength() {
        return 0;
    }
    
    @Override
    protected ModelPart getRepeatingPart() {
        return barrier;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 8F;
    }
}
