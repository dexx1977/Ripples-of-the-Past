package com.github.standobyte.jojo.potion;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

@Deprecated
public class UncurableEffect extends MobEffect {

    public UncurableEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

}
