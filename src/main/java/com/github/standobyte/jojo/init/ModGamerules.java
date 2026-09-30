package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.level.GameRules;

public class ModGamerules {
    
    public static void load() {}

    public static final GameRules.Key<GameRules.BooleanValue> BREAK_BLOCKS = GameRules.register(
            "jojoAbilitiesBreakBlocks", GameRules.Category.PLAYER, createBoolean(true));
    
    
    
    private static GameRules.Type<GameRules.BooleanValue> createBoolean(boolean defaultValue) {
        return CommonReflection.createBooleanGameRule(defaultValue);
    }
    
}
