package com.github.standobyte.jojo.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.render.item.ClackersISTER;
import com.github.standobyte.jojo.client.render.item.PolaroidISTER;
import com.github.standobyte.jojo.client.render.item.RoadRollerISTER;
import com.github.standobyte.jojo.client.render.item.TommyGunISTER;
import com.github.standobyte.jojo.client.render.item.CustomIconItem;
import com.github.standobyte.jojo.client.render.item.standdisc.StandDiscISTER;
import com.github.standobyte.jojo.init.ModItems;

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
        if (item == ModItems.CLACKERS.get()) return renderer(ClackersISTER::new);
        if (item == ModItems.TOMMY_GUN.get()) return renderer(TommyGunISTER::new);
        if (item == ModItems.ROAD_ROLLER.get()) return renderer(RoadRollerISTER::new);
        if (item == ModItems.METEORIC_SCRAP.get()) return renderer(CustomIconItem.DummyIconItemISTER::new);
        if (item == ModItems.STAND_DISC.get()) return renderer(StandDiscISTER::new);
        if (item == ModItems.POLAROID.get()) return renderer(PolaroidISTER::new);
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
