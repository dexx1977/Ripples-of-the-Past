package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.Collections;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.LivingEntity;

// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


public class PillarmanBladesModel<T extends LivingEntity> extends HumanoidModel<T> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

	public final ModelPart bladeRight;
	public final ModelPart bladeLeft;

	public PillarmanBladesModel(boolean slim) {
		super(ModelPart.humanoidRoot());
		texWidth = 16;
		texHeight = 16;


		// the arms are the vanilla humanoid parts, which are already baked with the
		// skin's texture size; the blades read their size from this model
		rightArm.cubes.clear();

		bladeRight = new ModelPart(this);
		bladeRight.setPos(-0.9F, 9.0F, 5.9F);
		ModelPart.addChild(rightArm, bladeRight);
		setRotationAngle(bladeRight, 0.0F, 3.1416F, 0.0F);
		bladeRight.texOffs(0, 0).addBox(0.2F, -2.8F, -1.0F, 1.0F, 3.0F, 5.0F, 0.0F, false);
		bladeRight.texOffs(0, 8).addBox(0.2F, -2.8F, -4.0F, 1.0F, 2.0F, 3.0F, 0.0F, false);
		bladeRight.texOffs(10, 0).addBox(0.2F, -2.8F, -6.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		bladeRight.texOffs(6, 11).addBox(0.2F, -3.8F, -7.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);

		// see above
		leftArm.cubes.clear();
		

		bladeLeft = new ModelPart(this);
		bladeLeft.setPos(2.3F, 9.0F, 5.9F);
		ModelPart.addChild(leftArm, bladeLeft);
		setRotationAngle(bladeLeft, 0.0F, 3.1416F, 0.0F);
		bladeLeft.texOffs(0, 0).addBox(0.2F, -2.8F, -1.0F, 1.0F, 3.0F, 5.0F, 0.0F, false);
		bladeLeft.texOffs(0, 8).addBox(0.2F, -2.8F, -4.0F, 1.0F, 2.0F, 3.0F, 0.0F, false);
		bladeLeft.texOffs(10, 0).addBox(0.2F, -2.8F, -6.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		bladeLeft.texOffs(6, 11).addBox(0.2F, -3.8F, -7.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
	}
	
	@Override
	protected Iterable<net.minecraft.client.model.geom.ModelPart> headParts() {
		return Collections.emptyList();
	}
	
	@Override
	protected Iterable<net.minecraft.client.model.geom.ModelPart> bodyParts() {
		return ImmutableList.of(this.rightArm, this.leftArm);
	}
	
	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}