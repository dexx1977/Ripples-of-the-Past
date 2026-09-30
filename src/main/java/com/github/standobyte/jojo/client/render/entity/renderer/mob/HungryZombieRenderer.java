package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.resources.ResourceLocation;

public class HungryZombieRenderer extends AbstractZombieRenderer<HungryZombieEntity, ZombieModel<HungryZombieEntity>> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/hungry_zombie.png");

    public HungryZombieRenderer(EntityRendererProvider.Context context) {
        // 1.20.1 bakes the base model and the two armor inflations as separate layers
        super(context, 
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), 
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return TEXTURE;
    }
}
