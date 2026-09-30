package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class GoldExperienceLifeformRevertMarker extends MarkerRenderer {
    
    public GoldExperienceLifeformRevertMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        return hud.getCurrentMode() == PowerClassification.STAND && hud.showExtraActionHud(ModStandsInit.GOLD_EXPERIENCE_REVERT_LIFEFORM.get())
                || hud.hasAvailableHotkey(ModStandsInit.GOLD_EXPERIENCE_REVERT_LIFEFORM.get());
    }
    
    @Override
    protected void renderIcon(PoseStack matrixStack, MarkerInstance marker, float partialTick) {
        marker.standEffect.ifPresent(effect -> {
            if (effect instanceof GECreatedLifeformEffect) {
                GECreatedLifeformEffect lifeformData = (GECreatedLifeformEffect) effect;
                ItemStack item = lifeformData.getItemView();
                if (item != null && !item.isEmpty()) {
                    renderItem(matrixStack, item, partialTick);
                }
                else {
                    Entity sourceEntity = lifeformData.getSource().getSourceEntity();
                    if (sourceEntity instanceof ObjectEntity) {
                        ObjectEntity.Type objectType = ((ObjectEntity) sourceEntity).getObjectType();
                        switch (objectType) {
                        case TOOTH:
                            mc.getTextureManager().bind(ICON_TOOTH);
                            AbstractGui.blit(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
                            break;
                        }
                    }
                }
            }
        });
    }
    
    private static final ResourceLocation ICON_TOOTH = new ResourceLocation(JojoMod.MOD_ID, "textures/icons/tooth.png");
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        GoldExperienceLifeformMarker.updateGELifeformMarkers(list, partialTick, mc, true);
    }
}
