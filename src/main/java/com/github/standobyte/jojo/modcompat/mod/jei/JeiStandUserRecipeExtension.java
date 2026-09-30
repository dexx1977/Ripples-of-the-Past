package com.github.standobyte.jojo.modcompat.mod.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.crafting.StandUserRecipe;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class JeiStandUserRecipeExtension implements ICraftingCategoryExtension {
    private final StandUserRecipe<?> recipe;
    private final JeiStandIconDrawable standIconDrawable;
    private static final int STAND_ICON_X = 62;
    private static final int STAND_ICON_Y = 2;
    
    public JeiStandUserRecipeExtension(StandUserRecipe<?> recipe) {
        this.recipe = recipe;
        this.standIconDrawable = new JeiStandIconDrawable(recipe.getStandTypesView());
    }
    
    /** The category lays the recipe out; this extension only draws its stand icons. */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {}
    
    @Override
    public void drawInfo(int recipeWidth, int recipeHeight, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        standIconDrawable.draw(guiGraphics, STAND_ICON_X, STAND_ICON_Y);
    }
    
    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (mouseX >= STAND_ICON_X && mouseX < STAND_ICON_X + standIconDrawable.getWidth() && 
                mouseY >= STAND_ICON_Y && mouseY < STAND_ICON_Y + standIconDrawable.getHeight()) {
            Collection<StandType<?>> stands = recipe.getStandTypesView();
            List<Component> lines = new ArrayList<>();
            
            if (!stands.isEmpty()) {
                if (stands.size() == 1) {
                    for (StandType<?> stand : stands) {
                        lines.add(Component.translatable("jojo.stand_user_crafting.jei_hint.single", stand.getName()));
                    }
                }
                else {
                    lines.add(Component.translatable("jojo.stand_user_crafting.jei_hint.multiple"));
                    for (StandType<?> stand : stands) {
                        lines.add(stand.getName());
                    }
                }
            }
            
            Collection<ResourceLocation> missingIds = recipe.getMissingIdsView();
            if (!missingIds.isEmpty()) {
                lines.add(Component.translatable("jojo.stand_user_crafting.jei_hint.error"));
                for (ResourceLocation id : missingIds) {
                    lines.add(Component.literal(id.toString()));
                }
            }
            
            lines.forEach(tooltip::add);
        }
    }
}
