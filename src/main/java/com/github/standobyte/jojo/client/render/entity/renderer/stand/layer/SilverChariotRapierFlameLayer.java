package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;
import java.util.function.Function;

import com.github.standobyte.jojo.client.render.entity.model.stand.SilverChariotRapierFlameLayerModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

public class SilverChariotRapierFlameLayer extends StandModelLayerRenderer<SilverChariotEntity, StandEntityModel<SilverChariotEntity>> {

    public SilverChariotRapierFlameLayer(SilverChariotRenderer entityRenderer) {
        super(entityRenderer, new SilverChariotRapierFlameLayerModel(), null);
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return LightTexture.pack(15, 15);
    }
    
    @Override
    public RenderType getRenderType(SilverChariotEntity entity) {
        return Sheets.translucentCullBlockSheet();
    }

    @Override
    public RenderType getRenderType(SilverChariotEntity entity, Function<ResourceLocation, RenderType> renderTYPE) {
        return Sheets.translucentCullBlockSheet();
        
    }

    @Deprecated
    @Override
    public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(SilverChariotEntity entity, Optional<ResourceLocation> standSkin) {
        return entity != null && entity.isRapierOnFire();
    }
}
