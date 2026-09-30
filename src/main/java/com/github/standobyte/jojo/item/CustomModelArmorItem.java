package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.client.render.armor.ArmorModelRegistry;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class CustomModelArmorItem extends ArmorItem {
    protected String textureStr;

    public CustomModelArmorItem(ArmorMaterial material, EquipmentSlot slot, Properties builder) {
        super(material, typeForSlot(slot), builder);
    }

    /** 1.20.1 describes the armour piece with a type instead of the slot. */
    private static net.minecraft.world.item.ArmorItem.Type typeForSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> net.minecraft.world.item.ArmorItem.Type.HELMET;
            case CHEST -> net.minecraft.world.item.ArmorItem.Type.CHESTPLATE;
            case LEGS -> net.minecraft.world.item.ArmorItem.Type.LEGGINGS;
            case FEET -> net.minecraft.world.item.ArmorItem.Type.BOOTS;
            default -> throw new IllegalArgumentException("Not an armour slot: " + slot);
        };
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(
                    net.minecraft.world.entity.LivingEntity entity, ItemStack stack, net.minecraft.world.entity.EquipmentSlot slot,
                    net.minecraft.client.model.HumanoidModel<?> original) {
                return com.github.standobyte.jojo.client.render.armor.ArmorModelRegistry.getModel(CustomModelArmorItem.this);
            }
        });
    }
    
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (textureStr == null) {
            textureStr = createTexturePath(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this));
        }
        return textureStr;
    }
    
    protected String createTexturePath(ResourceLocation regName) {
        return regName.getNamespace() + ":textures/armor/" + regName.getPath() + ".png";
    }
}