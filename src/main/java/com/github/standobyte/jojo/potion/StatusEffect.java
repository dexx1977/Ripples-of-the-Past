package com.github.standobyte.jojo.potion;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class StatusEffect extends MobEffect {
    private boolean isUncurable = false;

    public StatusEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends StatusEffect> T setUncurable() {
        this.isUncurable = true;
        return (T) this;
    }
    
    @Override
    public List<ItemStack> getCurativeItems() {
        if (isUncurable) {
            return Collections.emptyList();
        }
        return super.getCurativeItems();
    }
    
    public boolean isUncurable() {
        return isUncurable;
    }
}
