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
    public BakedModel resolve(BakedModel model, ItemStack item, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
        // 1.19 replaced Item#getItemStackTileEntityRenderer with the client item extensions
        BlockEntityWithoutLevelRenderer ister = net.minecraftforge.client.extensions.common.IClientItemExtensions.of(item).getCustomRenderer();
        if (ister instanceof ISTERWithEntity) {
            ((ISTERWithEntity) ister).setEntity(entity);
        }
        return model;
    }
}
