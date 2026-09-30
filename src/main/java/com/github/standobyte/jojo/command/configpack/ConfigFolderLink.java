package com.github.standobyte.jojo.command.configpack;

import java.io.IOException;
import java.nio.file.Path;

import com.github.standobyte.jojo.JojoMod;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

public class ConfigFolderLink implements IDataConfig {
    private static ConfigFolderLink instance;
    
    public static ConfigFolderLink init() {
        if (instance == null) {
            instance = new ConfigFolderLink();
        }
        return instance;
    }
    
    public static ConfigFolderLink getInstance() {
        return instance;
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> commandRegister(LiteralArgumentBuilder<CommandSourceStack> builder, String literal) {
        return builder.then(Commands.literal(literal)
                .executes(ctx -> sendDataPackLink(ctx.getSource()))
                );
    }
    
    private int sendDataPackLink(CommandSourceStack src) throws CommandSyntaxException {
        try {
            if (genDataPackBase(src)) {
                return 1;
            }
            else {
                Path packPath = dataPackPath(src.getServer());
                src.sendSuccess(Component.translatable("commands.jojoconfigpack.folder_link", 
                        Component.literal(getDataPackName()).withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> {
                            return style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, packPath.normalize().toString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                            Component.translatable("commands.jojoconfigpack.folder_link.tooltip", 
                                                    Component.literal("datapacks").withStyle(ChatFormatting.ITALIC)
                                                    )));
                        })
                        .withStyle(ChatFormatting.GRAY), true);
                return 0;
            }
        } catch (IOException e) {
            JojoMod.getLogger().error(e);
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(Component.literal(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    @Override
    public void syncToClient(ServerPlayer player) {}
}
