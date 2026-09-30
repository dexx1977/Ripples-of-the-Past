package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

// Made with Blockbench 3.9.2


public class BladeHatArmorModel extends HumanoidModel<LivingEntity> {
    // 1.20.1 has no ModelBase, so the texture size the parts are baked
    // against is declared here (as vanilla 1.20.1 models pass it to LayerDefinition).
    protected int texWidth = 64;
    protected int texHeight = 64;

    private final ModelPart hat;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;
    private final ModelPart cube_r3;
    private final ModelPart cube_r4;
    private final ModelPart cube_r5;
    private final ModelPart cube_r6;

    public BladeHatArmorModel(float size) {
        // 1.16.5's HumanoidModel(float) inflated every box; the helper bakes that mesh
        super(ModelPart.humanoidRoot(size));
        texWidth = 64;
        texHeight = 64;

        hat = new ModelPart(this);
        hat.setPos(0.0F, -4.0F, 0.0F);
        hat.texOffs(0, 23).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, false);
        hat.texOffs(32, 23).addBox(-4.0F, -6.1F, -4.0F, 8.0F, 4.0F, 8.0F, 0.25F, false);
        hat.texOffs(0, 12).addBox(-4.5F, -2.85F, -4.5F, 9.0F, 2.0F, 9.0F, -0.125F, false);
        hat.texOffs(36, 12).addBox(-3.0F, -2.85F, -4.6F, 6.0F, 2.0F, 1.0F, -0.1F, false);
        hat.texOffs(0, 37).addBox(-1.5F, -2.85F, -4.75F, 3.0F, 2.0F, 1.0F, 0.1F, false);
        hat.texOffs(0, 0).addBox(-4.0F, 0.0F, -6.0F, 8.0F, 0.0F, 12.0F, 0.0F, false);
        
        // the head is a vanilla baked part; the hat keeps the model texture size
        head.cubes = new java.util.ArrayList<>(); // baked cuboid lists are immutable in 1.20.1
        head.children.put("hat", hat);

        cube_r1 = new ModelPart(this);
        cube_r1.setPos(8.6198F, -1.9135F, 0.0F);
        ModelPart.addChild(hat, cube_r1);
        setRotationAngle(cube_r1, 0.0F, 0.0F, -0.3927F);
        cube_r1.texOffs(32, 35).addBox(-5.0F, 0.0F, -5.0F, 2.0F, 0.0F, 10.0F, 0.0F, true);

        cube_r2 = new ModelPart(this);
        cube_r2.setPos(-8.6198F, -1.9135F, 0.0F);
        ModelPart.addChild(hat, cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.0F, 0.3927F);
        cube_r2.texOffs(32, 35).addBox(3.0F, 0.0F, -5.0F, 2.0F, 0.0F, 10.0F, 0.0F, false);

        cube_r3 = new ModelPart(this);
        cube_r3.setPos(-4.5746F, -3.5345F, 0.9056F);
        ModelPart.addChild(hat, cube_r3);
        setRotationAngle(cube_r3, -0.3927F, 0.0F, -0.2182F);
        cube_r3.texOffs(8, 37).addBox(0.0F, -2.5F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, true);

        cube_r4 = new ModelPart(this);
        cube_r4.setPos(-4.5F, -3.6F, 0.0F);
        ModelPart.addChild(hat, cube_r4);
        setRotationAngle(cube_r4, -0.1745F, 0.0F, -0.2182F);
        cube_r4.texOffs(36, 15).addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, true);

        cube_r5 = new ModelPart(this);
        cube_r5.setPos(4.5746F, -3.5345F, 0.9056F);
        ModelPart.addChild(hat, cube_r5);
        setRotationAngle(cube_r5, -0.3927F, 0.0F, 0.2182F);
        cube_r5.texOffs(8, 37).addBox(0.0F, -2.5F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, false);

        cube_r6 = new ModelPart(this);
        cube_r6.setPos(4.5F, -3.6F, 0.0F);
        ModelPart.addChild(hat, cube_r6);
        setRotationAngle(cube_r6, -0.1745F, 0.0F, 0.2182F);
        cube_r6.texOffs(36, 15).addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, false);
    }
    
    @Override
    protected Iterable<net.minecraft.client.model.geom.ModelPart> bodyParts() {
        return Collections.emptyList();
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
    
    public static void modifyOuterLayer(PlayerModel<?> playerModel, LivingEntity entity) {
        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!headItem.isEmpty() && headItem.getItem() == ModItems.BLADE_HAT.get()) {
            playerModel.hat.visible = false;
        }
    }
}