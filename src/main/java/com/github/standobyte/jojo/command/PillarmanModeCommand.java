package com.github.standobyte.jojo.command;

import java.util.Collection;
import java.util.Optional;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class PillarmanModeCommand {
    private static final DynamicCommandExceptionType SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.pillarman.failed.single", player));
    private static final DynamicCommandExceptionType MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.pillarman.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pillarman").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.literal("stage").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stage", IntegerArgumentType.integer(1, PillarmanData.MAX_STAGE_LEVEL))
                                .executes(ctx -> setStage(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "stage"))))))
                        .then(Commands.literal("mode").then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("light")
                                        .executes(ctx -> setMode(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), PillarmanData.Mode.LIGHT)))
                                .then(Commands.literal("wind")
                                        .executes(ctx -> setMode(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), PillarmanData.Mode.WIND)))
                                .then(Commands.literal("heat")
                                        .executes(ctx -> setMode(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), PillarmanData.Mode.HEAT)))
                                .then(Commands.literal("none")
                                        .executes(ctx -> setMode(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), PillarmanData.Mode.NONE)))
                                ))
                ));
        JojoCommandsCommand.addCommand("pillarman");
    }

    private static int setStage(CommandSourceStack source, Collection<? extends ServerPlayer> targets, int stage) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayer player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<PillarmanData> pillarmanOptional = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get());
                pillarmanOptional.ifPresent(pillarman -> {
                    pillarman.setEvolutionStage(stage);
                });
                return pillarmanOptional.isPresent() ? 1 : 0;
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
            // the 1.20.1 sendSuccess takes a supplier, so the results have to be final
            final int successCount = success;
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.pillarman.stage.success.single", stage, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.pillarman.stage.success.multiple", stage, successCount), true);
            }
            return success;
        }
    }

    private static int setMode(CommandSourceStack source, Collection<? extends ServerPlayer> targets, int modeIndex) throws CommandSyntaxException {
        PillarmanData.Mode mode = PillarmanData.Mode.NONE; 
        if (modeIndex > 0 && modeIndex <= 3) mode = PillarmanData.Mode.values()[modeIndex];
        return setMode(source, targets, mode);
    }

    private static int setMode(CommandSourceStack source, Collection<? extends ServerPlayer> targets, PillarmanData.Mode mode) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayer player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<PillarmanData> pillarmanOptional = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get());
                pillarmanOptional.ifPresent(pillarman -> {
                	pillarman.setMode(mode);
                });
                return pillarmanOptional.isPresent() ? 1 : 0;
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
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.pillarman.mode.success.single", mode, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.pillarman.mode.success.multiple", mode, success), true);
            }
            return success;
        }
    }
}
