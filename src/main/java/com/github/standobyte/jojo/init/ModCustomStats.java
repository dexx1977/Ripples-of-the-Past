package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCustomStats {
    public static final ResourceLocation VAMPIRE_PEOPLE_DRAINED = new ResourceLocation(JojoMod.MOD_ID, "vampire_people_drained");
    public static final ResourceLocation VAMPIRE_ANIMALS_DRAINED = new ResourceLocation(JojoMod.MOD_ID, "vampire_animals_drained");
    public static final ResourceLocation VAMPIRE_ZOMBIES_CREATED = new ResourceLocation(JojoMod.MOD_ID, "vampire_zombies_created");
    public static final ResourceLocation VAMPIRE_ZOMBIES_SUMMONED = new ResourceLocation(JojoMod.MOD_ID, "vampire_zombies_summoned");
    public static final ResourceLocation RPS_WON = new ResourceLocation(JojoMod.MOD_ID, "rps_won");
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void registerCustomStats(net.minecraftforge.registries.RegisterEvent event) {
        if (!net.minecraft.core.registries.Registries.CUSTOM_STAT.equals(event.getRegistryKey())) {
            return;
        }
        registerCustomStat(VAMPIRE_PEOPLE_DRAINED, StatFormatter.DEFAULT);
        registerCustomStat(VAMPIRE_ANIMALS_DRAINED, StatFormatter.DEFAULT);
        registerCustomStat(VAMPIRE_ZOMBIES_CREATED, StatFormatter.DEFAULT);
        registerCustomStat(VAMPIRE_ZOMBIES_SUMMONED, StatFormatter.DEFAULT);
        registerCustomStat(RPS_WON, StatFormatter.DEFAULT);
    }

    private static ResourceLocation registerCustomStat(ResourceLocation resLoc, StatFormatter statFormatter) {
        Registry.register(BuiltInRegistries.CUSTOM_STAT, resLoc, resLoc);
        Stats.CUSTOM.get(resLoc, statFormatter);
        return resLoc;
    }
}
