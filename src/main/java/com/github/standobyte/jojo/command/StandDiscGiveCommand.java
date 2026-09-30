package com.github.standobyte.jojo.command;

import java.util.Collection;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.command.argument.StandArgument;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

public class StandDiscGiveCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("standdisc").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> giveStandDisc(ctx.getSource(), StandArgument.getStandType(ctx, "stand"), EntityArgument.getPlayers(ctx, "targets"))))))
                .then(Commands.literal("random").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> giveRandomStandDiscs(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                );
        JojoCommandsCommand.addCommand("standdisc");
    }
    
    private static int giveStandDisc(CommandSourceStack source, StandType<?> standType, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        return giveStandDiscItem(source, standType, targets);
    }
    
    private static int giveRandomStandDiscs(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        return giveStandDiscItem(source, null, targets);
    }
    
    /**
     * @param standType - if the argument is null, a random Stand is picked instead
     */
    private static int giveStandDiscItem(CommandSourceStack source, @Nullable StandType<?> standType, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        int i = 0;
        boolean random = standType == null;
        for (ServerPlayer player : targets) {
            if (random) {
                Either<StandType<?>, Component> randomStandOrError = StandUtil.randomStandOrError(player, player.getRandom());
                randomStandOrError.ifRight(error -> source.sendFailure(error));
                standType = randomStandOrError.left().orElse(null);
            }
            if (standType == null) {
                continue;
            }
            
            ItemStack discItem = createItemStack(standType);
            boolean added = player.inventory.add(discItem);
            if (added && discItem.isEmpty()) {
                discItem.setCount(1);
                ItemEntity itemEntity = player.drop(discItem, false);
                if (itemEntity != null) {
                    itemEntity.makeFakeItem();
                }

                player.level.playSound((Player) null, 
                        player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 
                        ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                player.inventoryMenu.broadcastChanges();
            } else {
                ItemEntity itemEntity = player.drop(discItem, false);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setThrower(player.getUUID());
                }
            }
            
            i++;
        }
        
        if (i > 0) {
            if (targets.size() == 1) {
                source.sendSuccess(Component.translatable("commands.give.success.single", 1, 
                        Component.translatable(ModItems.STAND_DISC.get().getDescriptionId()), targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(Component.translatable("commands.give.success.single", 1, 
                        Component.translatable(ModItems.STAND_DISC.get().getDescriptionId()), i), true);
            }
        }
        
        return i;
    }
    
    private static ItemStack createItemStack(StandType<?> standType) {
        return StandDiscItem.withStand(new ItemStack(ModItems.STAND_DISC.get()), new StandInstance(standType));
    }
}
