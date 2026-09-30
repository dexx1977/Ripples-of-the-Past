package com.github.standobyte.jojo.client.render.item.standdisc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class StandDiscOverrideList extends ItemOverrides {
    private final Map<ResourceLocation, BakedModel> cache = new HashMap<>();
    private final ItemOverrides wrappedOverrides;
    
    public StandDiscOverrideList(ItemOverrides wrappedOverrides) {
        this.wrappedOverrides = wrappedOverrides;
    }
    
    @Override
    public BakedModel resolve(BakedModel model, ItemStack item, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
        StandInstance discStand = StandDiscItem.getStandFromStack(item);
        if (discStand != null) {
            StandType<?> standType = discStand.getType();
            ItemModelShaper itemModelShaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
            BakedModel standSpecificModel = cache.computeIfAbsent(standType.getRegistryName(), standId -> itemModelShaper.getModelManager().getModel(makeStandSpecificModelPath(standType)));
            if (standSpecificModel != null && !ClientUtil.isMissingModel(standSpecificModel, itemModelShaper)) {
                model = standSpecificModel;
            }
        }
        
        return wrappedOverrides.resolve(model, item, world, entity, seed);
    }
    
    public static void onModelRegistry(net.minecraftforge.client.event.ModelEvent.RegisterAdditional event) {
        for (StandType<?> standType : JojoCustomRegistries.STANDS.getRegistry().getValues()) {
            event.register(new ModelResourceLocation(makeStandSpecificModelPath(standType), "inventory"));
        }
    }
    
    public static ResourceLocation makeStandSpecificModelPath(StandType<?> standType) {
        ResourceLocation id = standType.getRegistryName();
        return new ResourceLocation(id.getNamespace(), "item/stand_disc_" + id.getPath());
    }
    
}
