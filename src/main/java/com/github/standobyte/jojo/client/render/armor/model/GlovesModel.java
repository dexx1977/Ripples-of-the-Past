package com.github.standobyte.jojo.client.render.armor.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;

public class GlovesModel<T extends LivingEntity> extends PlayerModel<T> {

    public GlovesModel(float inflate, boolean slim) {
        super(inflate, slim);
    }

}
