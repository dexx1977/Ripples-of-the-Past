package com.github.standobyte.jojo.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.render.item.ClackersISTER;
import com.github.standobyte.jojo.client.render.item.polaroid.PolaroidISTER;
import com.github.standobyte.jojo.client.render.item.RoadRollerISTER;
import com.github.standobyte.jojo.client.render.item.tommygun.TommyGunISTER;
import com.github.standobyte.jojo.client.render.item.CustomIconItem;
import com.github.standobyte.jojo.client.render.item.standdisc.StandDiscISTER;
import com.github.standobyte.jojo.item.ClackersItem;
import com.github.standobyte.jojo.item.CustomIconItemBase;
import com.github.standobyte.jojo.item.PolaroidItem;
import com.github.standobyte.jojo.item.RoadRollerItem;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.item.TommyGunItem;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * The item renderers, which 1.16.5 attached through Item.Properties#setISTER.
 *
 * <p>1.20.1 hands them out from the item's client extensions instead. The mapping
 * lives here (a client class) so the item classes only ask for their own
 * extensions.</p>
 */
public class ClientItemRenderers {

    public static void initialize(Item item, Consumer<IClientItemExtensions> consumer) {
        IClientItemExtensions extensions = forItem(item);
        if (extensions != null) {
            consumer.accept(extensions);
        }
    }

    private static IClientItemExtensions forItem(Item item) {
        // this runs from the item's constructor, while the registry objects cannot be
        // read yet, so the items are matched by their class instead
        if (item instanceof ClackersItem) return renderer(ClackersISTER::new);
        if (item instanceof TommyGunItem) return renderer(TommyGunISTER::new);
        if (item instanceof RoadRollerItem) return renderer(RoadRollerISTER::new);
        if (item instanceof CustomIconItemBase) return renderer(CustomIconItem.DummyIconItemISTER::new);
        if (item instanceof StandDiscItem) return renderer(StandDiscISTER::new);
        if (item instanceof PolaroidItem) return renderer(PolaroidISTER::new);
        return null;
    }

    private static IClientItemExtensions renderer(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        };
    }
}
