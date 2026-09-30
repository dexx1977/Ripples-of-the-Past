package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RPSPvpGamesMap;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

public class RockPaperScissorsCommand {
    public static final String LITERAL = "rockpaperscissors";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(LITERAL).then(Commands.argument("target", EntityArgument.player())
                .executes(ctx -> game(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), ctx))));
        JojoCommandsCommand.addCommand(LITERAL);
    }
    
    private static int game(CommandSourceStack source, ServerPlayer opponent, CommandContext<CommandSourceStack> ctx) {
        Entity entity = source.getEntity();
        if (opponent.is(entity)) {
            ctx.getSource().sendFailure(Component.translatable("jojo.rps.self"));
            return 0;
        }
        if (entity instanceof ServerPlayer) {
            if (entity.distanceToSqr(opponent) >= 16) {
                ctx.getSource().sendFailure(Component.translatable("jojo.rps.too_far", opponent.getDisplayName()));
                return 0;
            }
            ServerPlayer player = (ServerPlayer) entity;
            RPSPvpGamesMap games = SaveFileUtilCapProvider.getSaveFileCap(opponent.server).getPvpRPSGames();
            RockPaperScissorsGame game = games.getOrCreateGame(player, opponent);
            game.getPlayer(player).setIsReady(true);
            if (game.getPlayer(opponent).isReady()) {
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
                opponent.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
                game.gameStarted(opponent.level());
            }
            else {
                String name = player.getGameProfile().getName();
                String command = "/" + LITERAL + " " + name;
                opponent.sendMessage(Component.translatable("jojo.rps.game_invite.text", player.getDisplayName(), 
                        Component.literal(command).withStyle(style -> {
                            return style.withColor(ChatFormatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(command)));
                        })), player.getUUID()); 
                ctx.getSource().sendSuccess(() -> Component.translatable("jojo.rps.game_invite.sent", opponent.getDisplayName()), false);
            }
            return 1;
        }
        return 0;
    }
}
