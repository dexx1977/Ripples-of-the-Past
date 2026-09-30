package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class LadybugBroochesModel<T extends LivingEntity> extends HumanoidModel<T> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    public final ModelPart broochRight;
    public final ModelPart broochLeft;
    public final ModelPart broochBottom;

    public LadybugBroochesModel() {
        super(0);
        
        texWidth = 64;
        texHeight = 64;

        body.cubes.clear();
        
        broochRight = new ModelPart(this);
        broochRight.setPos(-2.85F, 4.625F, -2.4F);
        ModelPart.addChild(body, broochRight);
        broochRight.texOffs(24, 2).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.6F, false);

        ModelPart broochPin1 = new ModelPart(this);
        broochPin1.setPos(0.0F, -0.375F, 0.4F);
        broochRight.addChild(broochPin1);
        broochPin1.xRot = 0.3927F;
        broochPin1.texOffs(34, 5).addBox(-0.5F, -0.35F, -0.65F, 1.0F, 2.0F, 1.0F, -0.35F, false);

        broochLeft = new ModelPart(this);
        broochLeft.setPos(2.85F, 4.625F, -2.4F);
        ModelPart.addChild(body, broochLeft);
        broochLeft.texOffs(24, 2).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.6F, false);

        ModelPart broochPin2 = new ModelPart(this);
        broochPin2.setPos(0.0F, -0.375F, 0.4F);
        broochLeft.addChild(broochPin2);
        broochPin2.xRot = 0.3927F;
        broochPin2.texOffs(34, 5).addBox(-0.5F, -0.35F, -0.65F, 1.0F, 2.0F, 1.0F, -0.35F, false);

        broochBottom = new ModelPart(this);
        broochBottom.setPos(0.05F, 10.125F, -2.4F);
        ModelPart.addChild(body, broochBottom);
        broochBottom.texOffs(24, 2).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.6F, false);

        ModelPart broochPin3 = new ModelPart(this);
        broochPin3.setPos(0.0F, -0.375F, 0.4F);
        broochBottom.addChild(broochPin3);
        broochPin3.xRot = 0.3927F;
        broochPin3.texOffs(34, 5).addBox(-0.5F, -0.35F, -0.65F, 1.0F, 2.0F, 1.0F, -0.35F, false);
    }
    
    @Override
    protected Iterable<net.minecraft.client.model.geom.ModelPart> bodyParts() {
        return ImmutableList.of(body);
    }

}
