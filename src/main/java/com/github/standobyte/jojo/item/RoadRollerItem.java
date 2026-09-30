package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class RoadRollerItem extends Item {
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.github.standobyte.jojo.client.ClientItemRenderers.initialize(this, consumer);
    }


    public RoadRollerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            RoadRollerEntity roadRoller = new RoadRollerEntity(world);
            roadRoller.copyPosition(player);
            world.addFreshEntity(roadRoller);
            player.startRiding(roadRoller);
            roadRoller.setOwner(player);
            if (IStandPower.getStandPowerOptional(player)
                    .map(stand -> stand.getType() == ModStands.THE_WORLD.getStandType())
                    .orElse(false)) {
                JojoModUtil.sayVoiceLine(player, ModSounds.DIO_ROAD_ROLLER.get());
            }
            if (!player.abilities.instabuild) {
                handStack.shrink(1);
            }
        }
        return InteractionResultHolder.consume(handStack);
    }

}
