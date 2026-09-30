package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class PolaroidItem extends Item {

    public PolaroidItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player cameraPlayer, InteractionHand hand) {
        ItemStack stack = cameraPlayer.getItemInHand(hand);
        
        if (!cameraPlayer.abilities.instabuild) {
            ItemStack paperItem = ItemStack.EMPTY;
            for (int i = 0; i < cameraPlayer.inventory.getContainerSize(); ++i) {
                ItemStack item = cameraPlayer.inventory.getItem(i);
                if (!item.isEmpty() && item.getItem() == Items.PAPER) {
                    paperItem = item;
                    break;
                }
            }
            
            if (paperItem.isEmpty()) {
                if (!world.isClientSide()) {
                    ((ServerPlayer) cameraPlayer).displayClientMessage(Component.translatable("jojo.polaroid.paper"), true);
                }
                return InteractionResultHolder.fail(stack);
            }
            
            if (!world.isClientSide()) {
                paperItem.shrink(1);
            }
        }
        
        if (world.isClientSide()) {
            switch (MCUtil.getHandSide(cameraPlayer, hand)) {
            case RIGHT:
                PolaroidHelper.takePicture(null, null, false, cameraPlayer.getId());
                break;
            case LEFT:
                float xRot = Math.max(-cameraPlayer.xRot, -82.5f);
                float yRot = cameraPlayer.yRot;
                float xRotAmount = Math.abs(xRot / 90);
                cameraPlayer.setYBodyRot(yRot + 45 * (1 - xRotAmount));
                cameraPlayer.yBodyRotO = cameraPlayer.yBodyRot;
                Vec3 lookVec = cameraPlayer.getLookAngle();
                Vec3 headUpVec = lookVec.xRot(90);
                Vec3 cameraPos = new Vec3(cameraPlayer.getX(), cameraPlayer.getEyeY() - 0.1, cameraPlayer.getZ());
                cameraPos = cameraPos.add(headUpVec.scale(xRotAmount * 0.2));
                cameraPos = cameraPos.add(lookVec.scale((1 - xRotAmount * 0.2)));
                PolaroidHelper.takePicture(cameraPos, 
                        angle -> new Vector3f(xRot, yRot - 165, 0), false, cameraPlayer.getId());
                break;
            }
        }
        else {
            cameraPlayer.getCooldowns().addCooldown(this, 60);
        }
        
        return InteractionResultHolder.consume(stack);
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("August_dr"));
    }
}
