package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.resources.ResourceLocation;

public class HungryZombieRenderer extends AbstractZombieRenderer<HungryZombieEntity, ZombieModel<HungryZombieEntity>> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/hungry_zombie.png");

    public HungryZombieRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager, new ZombieModel<>(0.0F, false), new ZombieModel<>(0.5F, true), new ZombieModel<>(1.0F, true));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return TEXTURE;
    }
}
