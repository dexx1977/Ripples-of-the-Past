package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.command.argument.ActionArgument;
import com.github.standobyte.jojo.command.argument.NonStandTypeArgument;
import com.github.standobyte.jojo.command.argument.StandArgument;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.RegisterEvent;

/**
 * 1.19 removed {@code ArgumentSerializer}; a custom argument type now has to be
 * registered in the vanilla {@code COMMAND_ARGUMENT_TYPE} registry (so the info
 * can be serialized to the client) and mapped to its class, which is what the
 * old {@code ArgumentTypeInfos.register(name, class, serializer)} did at once.
 */
@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModArgumentTypes {
    public static final SingletonArgumentInfo<StandArgument> STAND = SingletonArgumentInfo.contextFree(StandArgument::new);
    public static final SingletonArgumentInfo<NonStandTypeArgument> NON_STAND_TYPE = SingletonArgumentInfo.contextFree(NonStandTypeArgument::new);
    public static final SingletonArgumentInfo<ActionArgument> ACTION = SingletonArgumentInfo.contextFree(ActionArgument::new);

    @SubscribeEvent
    public static void registerArgumentTypes(RegisterEvent event) {
        if (!Registries.COMMAND_ARGUMENT_TYPE.equals(event.getRegistryKey())) {
            return;
        }
        register(event, "stand", STAND);
        register(event, "non_stand", NON_STAND_TYPE);
        register(event, "jojo_action", ACTION);
    }

    private static void register(RegisterEvent event, String name, ArgumentTypeInfo<?, ?> info) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, new ResourceLocation(JojoMod.MOD_ID, name), () -> info);
    }
}
