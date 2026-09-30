package com.github.standobyte.jojo.modcompat.mod.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.crafting.StandUserRecipe;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.plugins.vanilla.crafting.CraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class JeiStandUserRecipeExtension extends CraftingCategoryExtension<StandUserRecipe<?>> {
    private final JeiStandIconDrawable standIconDrawable;
    private static final int STAND_ICON_X = 62;
    private static final int STAND_ICON_Y = 2;
    
    public JeiStandUserRecipeExtension(StandUserRecipe<?> recipe) {
        super(recipe);
        this.standIconDrawable = new JeiStandIconDrawable(recipe.getStandTypesView());
    }
    
    @Override
    public void drawInfo(int recipeWidth, int recipeHeight, PoseStack matrixStack, double mouseX, double mouseY) {
        standIconDrawable.draw(matrixStack, STAND_ICON_X, STAND_ICON_Y);
    }
    
    @Override
    public List<Component> getTooltipStrings(double mouseX, double mouseY) {
        if (mouseX >= STAND_ICON_X && mouseX < STAND_ICON_X + standIconDrawable.getWidth() && 
                mouseY >= STAND_ICON_Y && mouseY < STAND_ICON_Y + standIconDrawable.getHeight()) {
            Collection<StandType<?>> stands = recipe.getStandTypesView();
            List<Component> tooltip = new ArrayList<>();
            
            if (!stands.isEmpty()) {
                if (stands.size() == 1) {
                    for (StandType<?> stand : stands) {
                        tooltip.add(Component.translatable("jojo.stand_user_crafting.jei_hint.single", stand.getName()));
                    }
                }
                else {
                    tooltip.add(Component.translatable("jojo.stand_user_crafting.jei_hint.multiple"));
                    for (StandType<?> stand : stands) {
                        tooltip.add(stand.getName());
                    }
                }
            }
            
            Collection<ResourceLocation> missingIds = recipe.getMissingIdsView();
            if (!missingIds.isEmpty()) {
                tooltip.add(Component.translatable("jojo.stand_user_crafting.jei_hint.error"));
                for (ResourceLocation id : missingIds) {
                    tooltip.add(Component.literal(id.toString()));
                }
            }
            
            return tooltip;
        }
        
        return super.getTooltipStrings(mouseX, mouseY);
    }
}
