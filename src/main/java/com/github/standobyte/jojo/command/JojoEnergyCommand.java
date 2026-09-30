package com.github.standobyte.jojo.command;

import java.util.Collection;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;

public class JojoEnergyCommand {
    private static final SimpleCommandExceptionType ERROR_SET_RATIO_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.jojoenergy.set.ratio.invalid"));
    private static final DynamicCommandExceptionType SET_SINGLE_EXCEPTION_NOT_LIVING = new DynamicCommandExceptionType(
            entity -> Component.translatable("commands.jojoenergy.set.failed.single.not_living", entity));
    private static final DynamicCommandExceptionType SET_MULTIPLE_EXCEPTION_NOT_LIVING = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.jojoenergy.set.failed.multiple.not_living", count));
    private static final DynamicCommandExceptionType SET_SINGLE_EXCEPTION_NO_POWER = new DynamicCommandExceptionType(
            entity -> Component.translatable("commands.jojoenergy.set.failed.single.no_power", entity));
    private static final DynamicCommandExceptionType SET_MULTIPLE_EXCEPTION_NO_POWER = new DynamicCommandExceptionType(
            count -> Component.translatable("commands.jojoenergy.set.failed.multiple.no_power", count));
    private static final DynamicCommandExceptionType GET_SINGLE_EXCEPTION_NOT_LIVING = new DynamicCommandExceptionType(
            player -> Component.translatable("commands.jojoenergy.get.failed.not_living", player));
    private static final DynamicCommandExceptionType GET_SINGLE_EXCEPTION_NO_POWER = new DynamicCommandExceptionType(
            entity -> Component.translatable("commands.jojoenergy.get.failed.no_power", entity));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jojoenergy").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                        .executes(ctx -> setEnergy(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), FloatArgumentType.getFloat(ctx, "amount"), NumType.VALUE))
                    .then(Commands.literal("points")
                        .executes(ctx -> setEnergy(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), FloatArgumentType.getFloat(ctx, "amount"), NumType.VALUE)))
                    .then(Commands.literal("ratio")
                        .executes(ctx -> setEnergy(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), FloatArgumentType.getFloat(ctx, "amount"), NumType.RATIO))))))
                .then(Commands.literal("get").then(Commands.argument("target", EntityArgument.entity())
                        .executes(ctx -> getEnergy(ctx.getSource(), EntityArgument.getEntity(ctx, "target")))))
                );
        JojoCommandsCommand.addCommand("jojoenergy");
    }
    
    private enum NumType {
        RATIO,
        VALUE
    }
    
    private static int setEnergy(CommandSourceStack source, Collection<? extends Entity> targets, float value, NumType numType) throws CommandSyntaxException {
        if (numType == NumType.RATIO && value > 1.0F) {
            throw ERROR_SET_RATIO_INVALID.create();
        }
        int i = 0;
        boolean hasLiving = false;
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity) {
                hasLiving = true;
                if (INonStandPower.getNonStandPowerOptional((LivingEntity) entity).map(power -> {
                    if (power.hasPower()) {
                        float amount;
                        switch (numType) {
                        case RATIO:
                            amount = value * power.getMaxEnergy();
                            break;
                        case VALUE:
                            amount = value;
                            break;
                        default:
                            return false;
                        }
                        power.setEnergy(amount);
                        return true;
                    }
                    return false;
                }).orElse(false)) {
                    i++;
                }
                else if (targets.size() == 1) {
                    throw SET_SINGLE_EXCEPTION_NO_POWER.create(targets.iterator().next().getName());
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                if (!hasLiving) {
                    throw SET_SINGLE_EXCEPTION_NOT_LIVING.create(targets.iterator().next().getName());
                }
                else {
                    throw SET_SINGLE_EXCEPTION_NO_POWER.create(targets.iterator().next().getName());
                }
            }
            else {
                if (!hasLiving) {
                    throw SET_MULTIPLE_EXCEPTION_NOT_LIVING.create(targets.size());
                }
                else {
                    throw SET_MULTIPLE_EXCEPTION_NO_POWER.create(targets.size());
                }
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable(
                        "commands.jojoenergy.set.success.single." + numType.toString().toLowerCase(), value, targets.iterator().next().getDisplayName()), true);
            }
            else {
                // the 1.20.1 sendSuccess takes a supplier, so the count has to be final
                final int successCount = i;
                source.sendSuccess(() -> Component.translatable(
                        "commands.jojoenergy.set.success.multiple." + numType.toString().toLowerCase(), value, successCount), true);
            }
            return i;
        }
    }
    
    private static int getEnergy(CommandSourceStack source, Entity target) throws CommandSyntaxException {
        if (target instanceof LivingEntity) {
            int energyInt = INonStandPower.getNonStandPowerOptional((LivingEntity) target).map(power -> {
                if (power.hasPower()) {
                    float energy = power.getEnergy();
                    float maxEnergy = power.getMaxEnergy();
                    source.sendSuccess(() -> Component.translatable("commands.jojoenergy.get.success", 
                            target.getDisplayName(), energy, maxEnergy, String.format("%.4f", energy / maxEnergy)), false);
                    return (int) energy;
                }
                else {
                    return -1;
                }
            }).orElse(-1);
            if (energyInt == -1) {
                throw GET_SINGLE_EXCEPTION_NO_POWER.create(target.getName());
            }
            return energyInt;
        }
        else {
            throw GET_SINGLE_EXCEPTION_NOT_LIVING.create(target.getName());
        }
    }
}
