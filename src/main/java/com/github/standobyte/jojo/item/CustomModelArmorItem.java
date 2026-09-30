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
        super(material, slot, builder);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <A extends HumanoidModel<?>> A getArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot armorSlot, A _default) {
        A model = (A) ArmorModelRegistry.getModel(this);
        return model;
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