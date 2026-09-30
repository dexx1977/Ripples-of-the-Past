package com.github.standobyte.jojo.client.render.block;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.client.resources.model.Material;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

public class BlockSprites {
    public static final Material MR_FIRE_BLOCK_0 = new Material(
            InventoryMenu.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "block/mr_fire_0"));
    public static final Material MR_FIRE_BLOCK_1 = new Material(
            InventoryMenu.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "block/mr_fire_1"));

}
