package com.github.standobyte.jojo.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public class StandLevelCommand {
    private static final DynamicCommandExceptionType STAND_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.stand.query.failed.single", player));
    private static final DynamicCommandExceptionType STAND_RESOLVE_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.stand.resolve.failed.single", player));
    private static final DynamicCommandExceptionType STAND_RESOLVE_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.stand.resolve.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("standlevel").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", IntegerArgumentType.integer(0))
                        .executes(ctx -> setStandLevel(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("levels", IntegerArgumentType.integer(0))
                        .executes(ctx -> addStandLevel(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "levels"))))))
                .then(Commands.literal("query").then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> getStandLevel(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))))
                );
        JojoCommandsCommand.addCommand("standlevel");
    }

    private static int getStandLevel(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        IStandPower stand = getStands(Util.make(new ArrayList<>(), list -> list.add(target))).iterator().next();
        int level = stand.getResolveLevel();
        source.sendSuccess(Component.translatable("commands.standlevel.query.success", target.getDisplayName(), level), false);
        return level;
    }

    private static int addStandLevel(CommandSourceStack source, Collection<? extends ServerPlayer> targets, int levels) throws CommandSyntaxException {
        Collection<IStandPower> stands = getStands(targets);
        for (IStandPower stand : stands) {
            stand.setResolveLevel(stand.getResolveLevel() + levels);
        }
        
        if (stands.size() == 1) {
            source.sendSuccess(Component.translatable("commands.standlevel.add.success.single", levels, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(Component.translatable("commands.standlevel.add.success.multiple", levels, stands.size()), true);
        }
        
        return stands.size();
    }

    private static int setStandLevel(CommandSourceStack source, Collection<? extends ServerPlayer> targets, int level) throws CommandSyntaxException {
        Collection<IStandPower> stands = getStands(targets);
        for (IStandPower stand : stands) {
            stand.setResolveLevel(level);
        }
        
        if (stands.size() == 1) {
            source.sendSuccess(Component.translatable("commands.standlevel.set.success.single", level, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(Component.translatable("commands.standlevel.set.success.multiple", level, stands.size()), true);
        }
        
        return stands.size();
    }
    
    private static Collection<IStandPower> getStands(Collection<? extends ServerPlayer> targets) throws CommandSyntaxException {
        List<IStandPower> stands = new ArrayList<>();
        boolean noStand = false;
        for (ServerPlayer player : targets) {
            IStandPower stand = IStandPower.getStandPowerOptional(player).orElse(null);
            if (stand == null || !stand.hasPower()) {
                noStand = true;
            }
            else if (stand.usesResolve()) {
                stands.add(stand);
            }
        }
        if (stands.isEmpty()) {
            if (targets.size() == 1) {
                if (noStand) {
                    throw STAND_SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
                }
                else {
                    throw STAND_RESOLVE_SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
                }
            }
            else {
                throw STAND_RESOLVE_MULTIPLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            }
        }
        else {
            return stands;
        }
    }
}
