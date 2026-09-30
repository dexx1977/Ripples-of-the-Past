package com.github.standobyte.jojo.command;

import java.util.Collection;

import com.github.standobyte.jojo.command.argument.NonStandTypeArgument;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class JojoPowerCommand {
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.non_stand.give.failed.single", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.non_stand.give.failed.multiple", count));
    private static final DynamicCommandExceptionType QUERY_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.non_stand.query.failed.single", player));
    private static final DynamicCommandExceptionType QUERY_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.non_stand.query.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jojopower").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("type", new NonStandTypeArgument())
                                .executes(ctx -> giveNonStandPowers(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), NonStandTypeArgument.getPowerType(ctx, "type"))))))
                .then(Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> removeNonStandPowers(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                );
        JojoCommandsCommand.addCommand("jojopower");
    }
    
    private static int giveNonStandPowers(CommandSourceStack source, Collection<ServerPlayer> targets, NonStandPowerType<?> powerType) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayer player : targets) {
            INonStandPower power = INonStandPower.getNonStandPowerOptional(player).orElse(null);
            if (power != null) {
                if (!power.hasPower() && power.givePower(powerType)) {
                    i++;
                    power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> vampirism.setVampireFullPower(true));
                }
                else if (targets.size() == 1) {
                    throw GIVE_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GIVE_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw GIVE_MULTIPLE_EXCEPTION.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(Component.translatable(
                        "commands.non_stand.give.success.single", 
                        Component.translatable(powerType.getTranslationKey()), targets.iterator().next().getDisplayName()), true);
            }
            else {
                source.sendSuccess(Component.translatable(
                        "commands.non_stand.give.success.multiple", 
                        Component.translatable(powerType.getTranslationKey()), i), true);
            }
            return i;
        }
    }

    private static int removeNonStandPowers(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        int i = 0;
        NonStandPowerType<?> removedPower = null;
        for (ServerPlayer player : targets) {
            INonStandPower power = INonStandPower.getNonStandPowerOptional(player).orElse(null);
            if (power != null) {
                NonStandPowerType<?> toBeRemoved = power.getType();
                if (power.clear()) {
                    i++;
                    removedPower = toBeRemoved;
                }
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
                source.sendSuccess(Component.translatable("commands.non_stand.remove.success.single", 
                        removedPower != null ? Component.translatable(removedPower.getTranslationKey()) : "", targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(Component.translatable("commands.non_stand.remove.success.multiple", i), true);
            }
            return i;
        }
    }
}
