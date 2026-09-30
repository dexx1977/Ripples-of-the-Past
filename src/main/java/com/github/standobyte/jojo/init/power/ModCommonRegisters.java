package com.github.standobyte.jojo.init.power;

import net.minecraft.resources.ResourceLocation;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraftforge.registries.DeferredRegister;

public class ModCommonRegisters {
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            new ResourceLocation(JojoMod.MOD_ID, "action"), JojoMod.MOD_ID);

    public static final DeferredRegister<NonStandPowerType<?>> NON_STAND_POWERS = DeferredRegister.create(
            new ResourceLocation(JojoMod.MOD_ID, "non_stand_type"), JojoMod.MOD_ID);
}
