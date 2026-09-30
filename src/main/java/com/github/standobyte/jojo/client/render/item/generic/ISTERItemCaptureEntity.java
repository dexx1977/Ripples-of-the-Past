package com.github.standobyte.jojo.client.render.item.generic;

import javax.annotation.Nullable;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ISTERItemCaptureEntity extends ItemOverrides {
    
    public ISTERItemCaptureEntity() {
        super();
    }
    
    @Override
    public BakedModel resolve(BakedModel model, ItemStack item, @Nullable ClientLevel world, @Nullable LivingEntity entity) {
        BlockEntityWithoutLevelRenderer ister = item.getItem().getItemStackTileEntityRenderer();
        if (ister instanceof ISTERWithEntity) {
            ((ISTERWithEntity) ister).setEntity(entity);
        }
        return model;
    }
}
