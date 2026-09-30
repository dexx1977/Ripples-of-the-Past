package com.github.standobyte.jojo.item;

import net.minecraft.world.item.Item;

/** The meteorite scrap, whose icon renderer lives in the client extensions. */
public class CustomIconItemBase extends Item {
    public CustomIconItemBase(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.github.standobyte.jojo.client.ClientItemRenderers.initialize(this, consumer);
    }
}
