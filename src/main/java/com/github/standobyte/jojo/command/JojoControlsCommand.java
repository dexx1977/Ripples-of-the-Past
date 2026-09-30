package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.util.mc.MCUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

public class JojoControlsCommand {
    public static final String LITERAL = "jojocontrols";
    private static final Component[] TEXT_PAGES = { 
            Component.translatable("jojo.chat.command.controls.overlay", 
                    Component.keybind("jojo.key.stand_mode").withStyle(ChatFormatting.ITALIC),
                    Component.keybind("jojo.key.non_stand_mode").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY),
            Component.translatable("jojo.chat.command.controls.overlay.scroll", 
                    Component.keybind("jojo.key.attack_hotbar").withStyle(ChatFormatting.ITALIC),
                    Component.keybind("jojo.key.ability_hotbar").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY),
            Component.translatable("jojo.chat.command.controls.overlay.use", 
                    Component.keybind("key.attack").withStyle(ChatFormatting.ITALIC),
                    Component.keybind("key.use").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY),
            Component.translatable("jojo.chat.command.controls.stand", 
                    Component.keybind("jojo.key.toggle_stand").withStyle(ChatFormatting.ITALIC),
                    Component.keybind("jojo.key.stand_remote_control").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY),
            Component.translatable("jojo.chat.command.controls.layout_edit", 
                    Component.keybind("jojo.key.jojo_menu").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY),
            Component.translatable("jojo.chat.command.controls.changeable").withStyle(ChatFormatting.GRAY)
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(LITERAL).executes((ctx -> {
            return writePage(0, ctx);
        })).then(Commands.argument("page", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).executes((ctx -> {
            return writePage(IntegerArgumentType.getInteger(ctx, "page"), ctx);
        }))));
        JojoCommandsCommand.addCommand(LITERAL);
    }

    private static int writePage(int page, CommandContext<CommandSourceStack> ctx) {
        page = Mth.clamp(page, 1, TEXT_PAGES.length);
        MutableComponent text = Component.translatable("jojo.chat.command.controls.page", String.valueOf(page), TEXT_PAGES.length).withStyle(ChatFormatting.DARK_GRAY);
        text.append(MCUtil.NEW_LINE);
        text.append(getPage(page - 1));
        if (page < TEXT_PAGES.length) {
            text.append(MCUtil.NEW_LINE);
        }
        if (page < TEXT_PAGES.length) {
            final int pageNext = page + 1;
            text.append(Component.translatable("jojo.chat.command.controls.next_page").withStyle((style) -> {
                return style.withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + JojoControlsCommand.LITERAL + " " + pageNext))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("jojo.chat.controls.tooltip")));
            }));
        }
        ctx.getSource().sendSuccess(text, false);
        return 0;
    }
    
    private static final Component getPage(int pageNum) {
        if (pageNum > TEXT_PAGES.length) {
            return Component.empty();
        }
        return TEXT_PAGES[pageNum];
    }
}
