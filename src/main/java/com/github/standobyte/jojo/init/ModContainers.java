package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.container.WalkmanItemContainer;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, JojoMod.MOD_ID);
    
    
    public static final RegistryObject<MenuType<WalkmanItemContainer>> WALKMAN = CONTAINERS.register("walkman", 
           () -> IForgeMenuType.create(WalkmanItemContainer::new));

}
