package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.stand.GoldExperienceMarkItem;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GoldExperienceMarkedItemMarker extends MarkerRenderer {
    
    public GoldExperienceMarkedItemMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
        return true;
    }
    
    @Override
    protected void renderIcon(PoseStack matrixStack, MarkerInstance marker, float partialTick) {
        ItemStack item = ((ItemMarkerInstance) marker).item;
        if (item != null && !item.isEmpty()) {
            renderItem(matrixStack, item, partialTick);
        }
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            List<Pair<GEItemMarkEffect, Vec3>> targets = GoldExperienceMarkItem.getTargets(stand, mc.player);
            Optional<GEItemMarkEffect> outlined = GoldExperienceMarkItem.getTargetedEffect(targets, mc.player);
            
            for (Pair<GEItemMarkEffect, Vec3> pair : targets) {
                GEItemMarkEffect effect = pair.getLeft();
                Vec3 pos = pair.getRight();
                TrackerItemStack item = effect.getItemTracker(false);
                list.add(new ItemMarkerInstance(pos, 
                        outlined.map(outlinedEffect -> pair.getLeft() == outlinedEffect).orElse(false),
                        item.getItem()));
            }
        });
    }
    
    
    private static class ItemMarkerInstance extends MarkerInstance {
        final ItemStack item;

        public ItemMarkerInstance(Vec3 pos, boolean outlined, ItemStack itemStack) {
            super(pos, outlined);
            this.item = itemStack;
        }
    }

}
