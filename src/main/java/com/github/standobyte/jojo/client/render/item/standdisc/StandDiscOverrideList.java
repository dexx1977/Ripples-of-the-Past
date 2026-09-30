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
        // 1.20.1 logs a warning for every requested model that does not exist, and the
        // mod itself only ships the generic disc model. The per stand models are meant
        // to come from resource packs, so only the ones that exist are requested (the
        // discs fall back to the generic model through the missing model check).
        net.minecraft.server.packs.resources.ResourceManager resources = Minecraft.getInstance().getResourceManager();
        for (StandType<?> standType : JojoCustomRegistries.STANDS.getRegistry().getValues()) {
            ResourceLocation path = makeStandSpecificModelPath(standType);
            ResourceLocation modelFile = new ResourceLocation(path.getNamespace(), "models/item/" + path.getPath() + ".json");
            if (resources.getResource(modelFile).isPresent()) {
                event.register(new ModelResourceLocation(path, "inventory"));
            }
        }
    }
    
    public static ResourceLocation makeStandSpecificModelPath(StandType<?> standType) {
        ResourceLocation id = standType.getRegistryName();
        // 1.20.1 resolves an inventory model as models/item/<path>.json, so the
        // location must not repeat the item folder
        return new ResourceLocation(id.getNamespace(), "stand_disc_" + id.getPath());
    }
    
}
