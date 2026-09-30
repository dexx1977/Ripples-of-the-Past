package com.github.standobyte.jojo.command;

import java.util.HashSet;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

public class JojoCommandsCommand {
    private static final Set<String> COMMANDS = new HashSet<>();
    private static MutableComponent finalText = Component.literal("");

    public static void addCommand(String literal) {
        COMMANDS.add(literal);
        finalText = 
                COMMANDS.stream()
                .sorted()
                .map(lit-> {
                    String command = "/" + lit;
                    return (MutableComponent) Component.translatable("jojo.command_description",  
                            Component.literal(command)
                            .withStyle(style -> {
                                return style.withColor(ChatFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(command)));
                            }), 
                            Component.translatable("jojo.command.desc." + lit));
                })
                .reduce((line1, line2) -> line1.append("\n").append(line2))
                .orElse(finalText);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jojocommands").executes((ctx -> {
            return writeContents(ctx);
        })));
    }

    private static int writeContents(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(finalText, false);
        return 0;
    }
}
