package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.item.walkman.WalkmanCassetteSlotCap;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanCassetteSlotProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.Playlist;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.TrackInfo;
import com.github.standobyte.jojo.container.WalkmanItemContainer;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

// FIXME cassettes can disappear from Walkman (WalkmanCassetteSlotCap)
public class WalkmanItem extends Item {

    public WalkmanItem(Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            editWalkmanData(stack, data -> data.initId((ServerLevel) world));
            stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(cap -> {
                if (cap instanceof WalkmanCassetteSlotCap) {
                    NetworkHooks.openScreen((ServerPlayer) player, (WalkmanCassetteSlotCap) cap, 
                            buf -> WalkmanItemContainer.writeAdditionalData(buf, stack));
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new WalkmanCassetteSlotProvider(stack);
    }
    
    
    public static Optional<WalkmanDataCap> getWalkmanData(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return MCUtil.nbtGetCompoundOptional(stack.getOrCreateTag(), "Walkman")
                .map(nbt -> {
                    WalkmanDataCap data = new WalkmanDataCap(stack);
                    data.fromNBT(nbt);
                    return data;
                });
    }
    
    public static void editWalkmanData(ItemStack recordedCassetteItem, Consumer<WalkmanDataCap> action) {
        WalkmanDataCap walkmanData = getWalkmanData(recordedCassetteItem).orElse(new WalkmanDataCap(recordedCassetteItem));
        action.accept(walkmanData);
        recordedCassetteItem.getOrCreateTag().put("Walkman", walkmanData.toNBT());
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (world != null) {
            getWalkmanData(stack).ifPresent(walkman -> {
                if (walkman.isIdInitialized()) {
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkman.getId());
                    if (playlist != null && playlist.isPlaying()) {
                        TrackInfo playingNow = playlist.getCurrentTrack();
                        if (playingNow != null) {
                            tooltip.add(Component.translatable("record.nowPlaying", playingNow.track.getName()).withStyle(ChatFormatting.GRAY));
                            tooltip.add(Component.literal(" "));
                        }
                    }
                }
            });
        }

        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }
}
