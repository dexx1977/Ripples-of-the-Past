package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import com.github.standobyte.jojo.client.render.FlameModelRenderer;
import com.github.standobyte.jojo.client.render.block.BlockSprites;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

// Made with Blockbench 3.9.2


public class MRRedBindModel extends RepeatingModel<MRRedBindEntity> {
    private final FlameModelRenderer flameRope;

    public MRRedBindModel() {
        texWidth = 32;
        texHeight = 32;

        flameRope = new FlameModelRenderer(this).setFireSprites(
                BlockSprites.MR_FIRE_BLOCK_0::sprite, 
                BlockSprites.MR_FIRE_BLOCK_1::sprite);
        flameRope.setPos(0.0F, 0.0F, 0.0F);
        flameRope.addFlame(0, 1.0F, 0, 2F, 3F, Direction.NORTH);
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
        return flameRope;
    }
    
    @Override
    protected float getRepeatingPartLength() {
        return 3F;
    }
}
