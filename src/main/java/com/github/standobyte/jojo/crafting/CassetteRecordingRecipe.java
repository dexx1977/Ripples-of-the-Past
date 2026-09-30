package com.github.standobyte.jojo.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModRecipeSerializers;
import com.github.standobyte.jojo.item.CassetteRecordedItem;
import com.github.standobyte.jojo.item.cassette.TrackSource;
import com.github.standobyte.jojo.item.cassette.TrackSource.TrackSourceType;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CassetteRecordingRecipe extends CustomRecipe {

    public CassetteRecordingRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CASSETTE_RECORD.get();
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        TrackRecording result = originalRecordingsAndCopyCount(inventory);
        return result != null && !result.trackSources.isEmpty() && result.copiesCount > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory) {
        TrackRecording result = originalRecordingsAndCopyCount(inventory);
        if (result != null && !result.trackSources.isEmpty() && result.copiesCount > 0) {
            ItemStack copies = new ItemStack(ModItems.CASSETTE_RECORDED.get(), result.copiesCount);
            CassetteRecordedItem.editCassetteData(copies, cap -> {
                cap.recordTracks(result.trackSources);
                result.ribbonColor.ifPresent(color -> cap.setDye(color));
            });
            return copies;
        }
        
        return ItemStack.EMPTY;
    }

    @Nullable
    private TrackRecording originalRecordingsAndCopyCount(CraftingContainer craftingGrid) {
        int blankCassettes = 0;
        List<TrackSource> musicSources = new ArrayList<>();
        Optional<DyeColor> color = null;

        for (int i = 0; i < craftingGrid.getContainerSize(); ++i) {
            ItemStack item = craftingGrid.getItem(i);
            if (!item.isEmpty()) {
                TrackSource trackSource = TrackSourceType.getMusicFromItem(item);
                if (trackSource != null) {
                    musicSources.add(trackSource);
                    
                    if (item.getItem() instanceof DyeItem) {
                        if (color == null) {
                            color = Optional.ofNullable(((DyeItem) item.getItem()).getDyeColor());
                        }
                        else {
                            color = Optional.empty();
                        }
                    }
                } 
                else {
                    if (item.getItem() != ModItems.CASSETTE_BLANK.get()) {
                        return null;
                    }
                    else {
                        ++blankCassettes;
                    }
                }
            }
        }

        if (color == null) color = Optional.empty();
        return new TrackRecording(musicSources, blankCassettes, color);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < items.size(); ++i) {
            ItemStack item = inventory.getItem(i);
            TrackSourceType type = TrackSourceType.getTrackSourceType(item);
            if (type != null && !type.isRecordingSourceItemSpent()) {
                ItemStack recordMaterial = item.copy();
                recordMaterial.setCount(1);
                items.set(i, recordMaterial);
            }
            else if (item.hasContainerItem()) {
                items.set(i, item.getContainerItem());
            }
        }

        return items;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }
    
    
    
    private class TrackRecording {
        private final List<TrackSource> trackSources;
        private final int copiesCount;
        private final Optional<DyeColor> ribbonColor;
        
        private TrackRecording(List<TrackSource> trackSources, int copiesCount, Optional<DyeColor> ribbonColor) {
            this.trackSources = trackSources;
            this.copiesCount = copiesCount;
            this.ribbonColor = ribbonColor;
        }
    }
}
