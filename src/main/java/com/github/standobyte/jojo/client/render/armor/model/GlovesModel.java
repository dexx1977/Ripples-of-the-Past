package com.github.standobyte.jojo.client.render.armor.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.LivingEntity;

public class GlovesModel<T extends LivingEntity> extends PlayerModel<T> {

    public GlovesModel(float inflate, boolean slim) {
        // 1.16.5's PlayerModel(float,boolean) inflated every box; 1.20.1 bakes that into the mesh
        super(createPlayerRoot(inflate, slim), slim);
    }

    private static net.minecraft.client.model.geom.ModelPart createPlayerRoot(float inflate, boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(inflate), slim), 64, 64).bakeRoot();
    }

}
