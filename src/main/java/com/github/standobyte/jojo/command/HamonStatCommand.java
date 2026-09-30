package com.github.standobyte.jojo.command;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class HamonStatCommand {
    private static final DynamicCommandExceptionType SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.hamon.failed.single", player));
    private static final DynamicCommandExceptionType MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.hamon.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hamonstat").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set")

                        .then(Commands.literal("strength").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_STAT_LEVEL))
                                .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.STRENGTH, true))
                                .then(Commands.argument("ignoreBreathing", BoolArgumentType.bool())
                                        .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.STRENGTH, BoolArgumentType.getBool(ctx, "ignoreBreathing")))))))
                        .then(Commands.literal("control").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_STAT_LEVEL))
                                .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.CONTROL, true))
                                .then(Commands.argument("ignoreBreathing", BoolArgumentType.bool())
                                        .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.CONTROL, BoolArgumentType.getBool(ctx, "ignoreBreathing")))))))
                        .then(Commands.literal("breathing").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_BREATHING_LEVEL))
                                .executes(ctx -> setBreathing(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level")))))))

                .then(Commands.literal("add")

                        .then(Commands.literal("strength").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_STAT_LEVEL))
                                .executes(ctx -> addHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.STRENGTH, true))
                                .then(Commands.argument("ignoreBreathing", BoolArgumentType.bool())
                                        .executes(ctx -> addHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.STRENGTH, BoolArgumentType.getBool(ctx, "ignoreBreathing")))))))
                        .then(Commands.literal("control").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_STAT_LEVEL))
                                .executes(ctx -> addHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.CONTROL, true))
                                .then(Commands.argument("ignoreBreathing", BoolArgumentType.bool())
                                        .executes(ctx -> addHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"), HamonStat.CONTROL, BoolArgumentType.getBool(ctx, "ignoreBreathing")))))))
                        .then(Commands.literal("breathing").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_BREATHING_LEVEL))
                                .executes(ctx -> addBreathing(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level")))))))

                .then(Commands.literal("query")

                        .then(Commands.literal("strength").then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> getHamonStat(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), HamonStat.STRENGTH))))
                        .then(Commands.literal("control").then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> getHamonStat(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), HamonStat.CONTROL))))
                        .then(Commands.literal("breathing").then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> getBreathing(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))))
                ));
        JojoCommandsCommand.addCommand("hamonstat");
    }

    private static int setHamonStat(CommandSourceStack source, Collection<? extends ServerPlayer> targets, float level, HamonStat stat, boolean ignoreBreathing) throws CommandSyntaxException {
        return setHamonStat(source, targets, (current, arg) -> arg, level, stat, ignoreBreathing, "");
    }

    private static int addHamonStat(CommandSourceStack source, Collection<? extends ServerPlayer> targets, float level, HamonStat stat, boolean ignoreBreathing) throws CommandSyntaxException {
        return setHamonStat(source, targets, Float::sum, level, stat, ignoreBreathing, "add.");
    }

    private static int setBreathing(CommandSourceStack source, Collection<? extends ServerPlayer> targets, float level) throws CommandSyntaxException {
        return setBreathing(source, targets, (current, arg) -> arg, level, "");
    }

    private static int addBreathing(CommandSourceStack source, Collection<? extends ServerPlayer> targets, float level) throws CommandSyntaxException {
        return setBreathing(source, targets, Float::sum, level, "add.");
    }

    private static int setHamonStat(CommandSourceStack source, Collection<? extends ServerPlayer> targets, BinaryOperator<Float> operation, float level, HamonStat stat, boolean ignoreBreathing, String msg) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayer player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
                hamonOptional.ifPresent(hamon -> {
                    int curPoints = stat == HamonStat.STRENGTH ? hamon.getHamonStrengthPoints() : hamon.getHamonControlPoints();
                    float curLevel = HamonData.levelFractionFromPoints(curPoints);
                    float levelToSet = operation.apply(curLevel, level);
                    int pointsToSet = HamonData.pointsAtLevelFraction(levelToSet);
                    hamon.setHamonStatPoints(stat, pointsToSet, ignoreBreathing, true);
                    hamon.tcsa(false);
                });
                return hamonOptional.isPresent() ? 1 : 0;
            }).orElse(0);
        }
        if (success == 0) {
            if (targets.size() == 1) {
                throw SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw MULTIPLE_FAILED_EXCEPTION.create(targets.size());
            }
        }
        else {
            // the 1.20.1 sendSuccess takes a supplier, so the result count has to be final
            final int successCount = success;
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable(stat == HamonStat.STRENGTH ? "commands.hamon.strength." + msg + "success.single" : "commands.hamon.control." + msg + "success.single", 
                        level, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable(stat == HamonStat.STRENGTH ? "commands.hamon.strength." + msg + "success.multiple" : "commands.hamon.control." + msg + "success.multiple", 
                        level, successCount), true);
            }
            return success;
        }
    }

    private static int setBreathing(CommandSourceStack source, Collection<? extends ServerPlayer> targets, BinaryOperator<Float> operation, float level, String msg) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayer player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
                hamonOptional.ifPresent(hamon -> {
                    float levelToSet = operation.apply(hamon.getBreathingLevel(), level);
                    hamon.setBreathingLevel(levelToSet);
                });
                return hamonOptional.isPresent() ? 1 : 0;
            }).orElse(0);
        }
        if (success == 0) {
            if (targets.size() == 1) {
                throw SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw MULTIPLE_FAILED_EXCEPTION.create(targets.size());
            }
        }
        else {
            final int successCount = success;
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.hamon.breathing." + msg + "success.single", level, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.hamon.breathing." + msg + "success.multiple", level, successCount), true);
            }
            return success;
        }
    }
    
    private static int getHamonStat(CommandSourceStack source, ServerPlayer target, HamonStat stat) throws CommandSyntaxException {
        Optional<HamonData> playerHamon = INonStandPower.getNonStandPowerOptional(target).resolve().flatMap(
                power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
        if (playerHamon.isPresent()) {
            float level = playerHamon.get().getStatLevel(stat);
            source.sendSuccess(() -> Component.translatable(stat == HamonStat.STRENGTH ? "commands.hamon.strength.query.success" : "commands.hamon.control.query.success", 
                    target.getDisplayName(), new DecimalFormat("#.##").format(level)), false);
            return (int) level;
        }
        else {
            throw SINGLE_FAILED_EXCEPTION.create(target);
        }
    }
    
    private static int getBreathing(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        Optional<HamonData> playerHamon = INonStandPower.getNonStandPowerOptional(target).resolve().flatMap(
                power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
        if (playerHamon.isPresent()) {
            float level = playerHamon.get().getBreathingLevel();
            source.sendSuccess(() -> Component.translatable("commands.hamon.breathing.query.success", 
                    target.getDisplayName(), new DecimalFormat("#.##").format(level)), false);
            return (int) level;
        }
        else {
            throw SINGLE_FAILED_EXCEPTION.create(target);
        }
    }

}
