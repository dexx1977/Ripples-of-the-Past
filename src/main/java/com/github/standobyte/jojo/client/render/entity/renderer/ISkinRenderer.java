package com.github.standobyte.jojo.client.render.entity.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public interface ISkinRenderer<T extends Entity, M extends EntityModel<T>> {
    int getSkin(T entity);
    M getModel(int skin);
    ResourceLocation getTexture(int skin);
}
