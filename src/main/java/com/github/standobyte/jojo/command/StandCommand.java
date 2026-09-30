package com.github.standobyte.jojo.command;

import java.util.Collection;

import com.github.standobyte.jojo.command.argument.StandArgument;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class StandCommand {
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION_ALREADY_HAS = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.stand.give.failed.single.has", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION_ALREADY_HAVE = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.stand.give.failed.multiple.have", count));
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION_RANDOM = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.stand.give.failed.single.random", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION_RANDOM = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.stand.give.failed.multiple.random", count));
    private static final DynamicCommandExceptionType QUERY_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.stand.query.failed.single", player));
    private static final DynamicCommandExceptionType QUERY_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.stand.query.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stand").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> giveStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StandArgument.getStandType(ctx, "stand"), false))
                        .then(Commands.argument("replace", BoolArgumentType.bool())
                                .executes(ctx -> giveStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StandArgument.getStandType(ctx, "stand"), BoolArgumentType.getBool(ctx, "replace")))))))
                .then(Commands.literal("random").then(Commands.argument("targets", EntityArgument.players()) // /stand random <player(s)>
                        .executes(ctx -> giveRandomStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), false))
                        .then(Commands.argument("replace", BoolArgumentType.bool())
                                .executes(ctx -> giveRandomStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), BoolArgumentType.getBool(ctx, "replace"))))))
                .then(Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> removeStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("type").then(Commands.argument("targets", EntityArgument.player())
                        .executes(ctx -> queryStand(ctx.getSource(), EntityArgument.getPlayer(ctx, "targets")))))
                );
        JojoCommandsCommand.addCommand("stand");
    }

    private static int giveStands(CommandSourceStack source, Collection<ServerPlayer> targets, StandType<?> standType, boolean replace) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayer player : targets) {
            IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
            if (power != null) {
                if (replace) {
                    power.clear();
                }
                if (power.givePower(standType)) {
                    i++;
                }
                else if (targets.size() == 1) {
                    throw GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(targets.iterator().next().getName());
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(targets.iterator().next().getName());
            } else {
                throw GIVE_MULTIPLE_EXCEPTION_ALREADY_HAVE.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable(
                        "commands.stand.give.success.single", 
                        standType.getName(), targets.iterator().next().getDisplayName()), true);
            }
            else {
                // the 1.20.1 sendSuccess takes a supplier, so the count has to be final
                final int successCount = i;
                source.sendSuccess(() -> Component.translatable(
                        "commands.stand.give.success.multiple", 
                        standType.getName(), successCount), true);
            }
            return i;
        }
    }
    
    private static int giveRandomStands(CommandSourceStack source, Collection<ServerPlayer> targets, boolean replace) throws CommandSyntaxException {
        int i = 0;
        if (!targets.isEmpty()) {
            for (ServerPlayer player : targets) {
                Component errorMessage = null;
                IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
                if (power != null) {
                    Either<StandType<?>, Component> standOrError = null;
                    if (!replace && power.hasPower()) {
                        errorMessage = (Component) GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(player.getName()).getRawMessage();
                    }
                    else {
                        standOrError = StandUtil.randomStandOrError(player, player.getRandom());
                        if (standOrError.right().isPresent()) {
                            errorMessage = Component.translatable("commands.list.nameAndId", 
                                    GIVE_SINGLE_EXCEPTION_RANDOM.create(player.getName()).getRawMessage(), 
                                    standOrError.right().get());
                        }
                    }
                    
                    if (errorMessage != null) {
                        source.sendFailure(errorMessage);
                        continue;
                    }
                    
                    StandType<?> stand = standOrError.left().orElse(null);
                    if (stand != null) {
                        if (replace) {
                            power.clear();
                        }
                        if (power.givePower(stand)) {
                            i++;
                        }
                    }
                }
            }
        }
        if (i > 0) {
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.stand.give.success.single.random", 
                        targets.iterator().next().getDisplayName()), true);
            }
            else {
                final int successCount = i;
                source.sendSuccess(() -> Component.translatable("commands.stand.give.success.multiple.random", successCount), true);
            }
        }
        return i;
    }

    private static int removeStands(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        int i = 0;
        StandType<?> removedStand = null;
        for (ServerPlayer player : targets) {
            IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
            if (power != null) {
                removedStand = power.getType();
                power.clear();
                power.fullStandClear();
                i++;
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw QUERY_SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw QUERY_MULTIPLE_FAILED_EXCEPTION.create(targets.size());
            }
        } else {
            if (targets.size() == 1) {
                Component message;
                if (removedStand != null) {
                    message = Component.translatable("commands.stand.remove.success.single", 
                            removedStand.getName(), targets.iterator().next().getDisplayName());
                }
                else {
                    message = Component.translatable("commands.stand.remove.success.single.no_stand", 
                            targets.iterator().next().getDisplayName());
                }
                final Component removeMessage = message;
                source.sendSuccess(() -> removeMessage, true);
            } else {
                final int successCount = i;
                source.sendSuccess(() -> Component.translatable("commands.stand.remove.success.multiple", successCount), true);
            }
            return i;
        }
    }

    private static int queryStand(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
        if (power != null) {
            if (power.hasPower()) {
                StandType<?> type = power.getType();
                source.sendSuccess(() -> Component.translatable("commands.stand.query.success", player.getDisplayName(), type.getName()), false);
                return JojoCustomRegistries.STANDS.getNumericId(type.getRegistryName());
            }
        }
        throw QUERY_SINGLE_FAILED_EXCEPTION.create(player.getName());
    }
}
