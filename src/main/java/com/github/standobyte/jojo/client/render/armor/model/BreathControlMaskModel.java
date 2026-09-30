package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import net.minecraft.client.model.HumanoidModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.LivingEntity;

// Made with Blockbench 3.9.2


public class BreathControlMaskModel extends HumanoidModel<LivingEntity> {

    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    public BreathControlMaskModel(float size) {
        super(size);
        texWidth = 32;
        texHeight = 32;

        head.setTexSize(texWidth, texHeight);
        head.cubes.clear();
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 3.0F, 3.0F, 0.4F, false);
        head.texOffs(22, 0).addBox(-1.0F, -2.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.6F, false);
    }
    
    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Collections.emptyList();
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}