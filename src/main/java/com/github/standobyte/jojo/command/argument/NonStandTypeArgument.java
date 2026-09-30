package com.github.standobyte.jojo.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import com.github.standobyte.jojo.init.ModArgumentTypes;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.network.chat.Component;
import com.github.standobyte.jojo.init.power.RegistryEntry;

public class NonStandTypeArgument implements ArgumentType<NonStandPowerType<?>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("hamon", "vampirism");
    public static final DynamicCommandExceptionType TYPE_UNKNOWN = new DynamicCommandExceptionType((key) -> Component.translatable("non_stand.unknown", key));
    private static Map<String, List<NonStandPowerType<?>>> BY_LOCATION;
    
    public static void commonSetupRegister() {
        // the info itself is registered on the mod bus, this is the class -> info mapping
        ArgumentTypeInfos.registerByClass(NonStandTypeArgument.class, ModArgumentTypes.NON_STAND_TYPE);
        BY_LOCATION = ArgumentUtil.groupByKeyLocation(JojoCustomRegistries.NON_STAND_POWERS.getRegistry());
    }
    
    @Override
    public NonStandPowerType<?> parse(StringReader reader) throws CommandSyntaxException {
        String input = ArgumentUtil.read(reader);
        return ArgumentUtil.bestFittingValue(input, JojoCustomRegistries.NON_STAND_POWERS.getRegistry(), BY_LOCATION, TYPE_UNKNOWN);
    }
    
    public static <S> NonStandPowerType<?> getPowerType(CommandContext<S> context, String name) {
        return context.getArgument(name, NonStandPowerType.class);
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return ArgumentUtil.suggestIterable(JojoCustomRegistries.NON_STAND_POWERS.getRegistry().getValues()
                .stream().map(RegistryEntry::getRegistryName), builder);
    }
    
    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
