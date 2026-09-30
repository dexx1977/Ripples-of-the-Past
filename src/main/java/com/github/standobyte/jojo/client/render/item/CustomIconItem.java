package com.github.standobyte.jojo.client.render.item;

import net.minecraft.world.item.ItemDisplayContext;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientSetup;
import com.github.standobyte.jojo.client.render.item.generic.CustomModelItemISTER;
import com.github.standobyte.jojo.client.render.item.generic.ItemISTERModelWrapper;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.model.Model;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import com.mojang.math.Axis;
import net.minecraft.nbt.Tag;
import com.github.standobyte.jojo.util.mc.MCUtil;

public class CustomIconItem {
    public static final Supplier<Item> DUMMY_ITEM = ModItems.METEORIC_SCRAP;

    
    public static final ItemStack makeIconItem(RegularIcon icon) {
        ItemStack iconItem = new ItemStack(DUMMY_ITEM.get());
        iconItem.getOrCreateTag().putInt("Icon", icon.overrideValue);
        return iconItem;
    }
    
    public static final ItemStack makeIconItem(CustomModelIcon icon) {
        ItemStack iconItem = new ItemStack(DUMMY_ITEM.get());
        CompoundTag nbt = iconItem.getOrCreateTag();
        nbt.putInt("CustomModel", icon.ordinal() + 1);
        return iconItem;
    }
    
    public static enum RegularIcon {
        KATAKANA_GO(1),
        CRIMSON_BUBBLE(2),
        VAMPIRISM_FREEZE(3),
        STAR_PLATINUM_BARRAGE(4),
        RESOLVE_FULL(5),
        RESOLVE_EFFECT(6),
        TIME_STOP(7),
        SP_TRANSLUCENT(8),
        SP(9),
        SOUL_CLOUD(10),
        RPS_ICONS(11),
        MR_FIREBALL(12),
        STEVE_MUSCLE(13),
        AWAKEN(14),
        WIND_MODE(15),
        HEAT_MODE(16),
        LIGHT_MODE(17),
        PILLAR_MAN_PUNCH(18),
        VAMPIRISM_PUNCH(19),
        HAMON_PUNCH(20),
        PILLAR_MAN_EXPLODE(21);
        
        private final int overrideValue;
        
        private RegularIcon(int overrideValue) {
            this.overrideValue = overrideValue;
        }
    }
    
    public static enum CustomModelIcon {
        MOD_LOGO(
//                new ResourceLocation(JojoMod.MOD_ID, "mod_logo"),
                () -> () -> new CustomModelItemISTER<>(
                        new ResourceLocation(JojoMod.MOD_ID, "mod_logo"), 
                        new ResourceLocation(JojoMod.MOD_ID, "textures/mod_logo_model.png"), 
                        DUMMY_ITEM, 
                        ModLogoModel::new));
        
//        private final ResourceLocation vanillaModelTransforms;
        private final Supplier<Callable<BlockEntityWithoutLevelRenderer>> isterSupplier;
        private Supplier<BlockEntityWithoutLevelRenderer> ister;
        
        private CustomModelIcon(
//                ResourceLocation modelWithTransforms, 
                Supplier<Callable<BlockEntityWithoutLevelRenderer>> ister) {
//            this.vanillaModelTransforms = modelWithTransforms;
            this.isterSupplier = ister;
        }
    }

    
    
    public static void registerModelOverride() {
        ItemProperties.register(DUMMY_ITEM.get(), 
                new ResourceLocation(JojoMod.MOD_ID, "icon"), 
                (itemStack, clientWorld, livingEntity) -> {
                    return itemStack.getOrCreateTag().getInt("Icon");
                });
    }
    
    public static void onModelBake(Map<ResourceLocation, BakedModel> modelRegistry) {
        ClientSetup.registerCustomBakedModel(MCUtil.id(DUMMY_ITEM.get()), modelRegistry, 
                model -> new ItemISTERModelWrapper(model));
    }
    
    
    
    public static class DummyIconItemISTER extends BlockEntityWithoutLevelRenderer {
        
        public DummyIconItemISTER() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            for (CustomModelIcon icon : CustomModelIcon.values()) {
                try {
                    BlockEntityWithoutLevelRenderer ister = icon.isterSupplier.get().call();
                    icon.ister = () -> ister;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // TODO display transform
        @Override
        public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack matrixStack, 
                MultiBufferSource renderTypeBuffer, int light, int overlay) {
            if (itemStack.hasTag()) {
                CompoundTag nbt = itemStack.getTag();
                if (nbt.contains("CustomModel", Tag.TAG_INT)) {
                    int modelOrdinal = nbt.getInt("CustomModel") - 1;
                    CustomModelIcon values[] = CustomModelIcon.values();
                    if (modelOrdinal >= 0 && modelOrdinal < values.length) {
                        CustomModelIcon model = values[modelOrdinal];
                        model.ister.get().renderByItem(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
                        return;
                    }
                }
            }
            BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null);
            CustomModelItemISTER.renderItemNormally(matrixStack, itemStack, 
                    transformType, renderTypeBuffer, light, overlay, itemModel);
        }
    }
    
    protected static class ModLogoModel extends Model {
        private ModelPart root;

        public ModLogoModel() {
            super(RenderType::entityCutoutNoCull);
        }

        @Override
        public void renderToBuffer(PoseStack pMatrixStack, 
                VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay,
                float pRed, float pGreen, float pBlue, float pAlpha) {
            if (root != null) {
                pMatrixStack.pushPose();
                pMatrixStack.scale(0.75f, 0.75f, 0.75f);
                pMatrixStack.translate(0, 0.5f, 0);
                Matrix3f lighting = pMatrixStack.last().normal();
                lighting.mul(Axis.YP.rotationDegrees(-45));
                lighting.mul(Axis.XP.rotationDegrees(-45));
                lighting.mul(Axis.ZP.rotationDegrees(45));
                root.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
                pMatrixStack.popPose();
            }
        }
    }
    
}