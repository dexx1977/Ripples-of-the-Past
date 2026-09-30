package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.Collections;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


public class PillarmanBladesModel<T extends LivingEntity> extends HumanoidModel<T> {
	public final ModelPart bladeRight;
	public final ModelPart bladeLeft;

	public PillarmanBladesModel(boolean slim) {
		super(0);
		texWidth = 16;
		texHeight = 16;


		rightArm.setTexSize(texWidth, texHeight);
		rightArm.cubes.clear();

		bladeRight = new ModelPart(this);
		bladeRight.setPos(-0.9F, 9.0F, 5.9F);
		rightArm.addChild(bladeRight);
		setRotationAngle(bladeRight, 0.0F, 3.1416F, 0.0F);
		bladeRight.texOffs(0, 0).addBox(0.2F, -2.8F, -1.0F, 1.0F, 3.0F, 5.0F, 0.0F, false);
		bladeRight.texOffs(0, 8).addBox(0.2F, -2.8F, -4.0F, 1.0F, 2.0F, 3.0F, 0.0F, false);
		bladeRight.texOffs(10, 0).addBox(0.2F, -2.8F, -6.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		bladeRight.texOffs(6, 11).addBox(0.2F, -3.8F, -7.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);

		leftArm.setTexSize(texWidth, texHeight);
		leftArm.cubes.clear();
		

		bladeLeft = new ModelPart(this);
		bladeLeft.setPos(2.3F, 9.0F, 5.9F);
		leftArm.addChild(bladeLeft);
		setRotationAngle(bladeLeft, 0.0F, 3.1416F, 0.0F);
		bladeLeft.texOffs(0, 0).addBox(0.2F, -2.8F, -1.0F, 1.0F, 3.0F, 5.0F, 0.0F, false);
		bladeLeft.texOffs(0, 8).addBox(0.2F, -2.8F, -4.0F, 1.0F, 2.0F, 3.0F, 0.0F, false);
		bladeLeft.texOffs(10, 0).addBox(0.2F, -2.8F, -6.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		bladeLeft.texOffs(6, 11).addBox(0.2F, -3.8F, -7.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
	}
	
	@Override
	protected Iterable<ModelPart> headParts() {
		return Collections.emptyList();
	}
	
	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.rightArm, this.leftArm);
	}
	
	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}