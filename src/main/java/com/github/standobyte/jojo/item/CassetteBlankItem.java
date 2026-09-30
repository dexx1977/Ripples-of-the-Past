package com.github.standobyte.jojo.item;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.item.cassette.TrackSourceDye;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

public class CassetteBlankItem extends Item {

    public CassetteBlankItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        boolean hasDyes = Arrays.stream(DyeColor.values())
                .anyMatch(dye -> {
                    TrackSourceDye source = new TrackSourceDye(dye);
                    return WalkmanSoundHandler.CassetteTracksSided.getTracks(source)
                            .findAny().isPresent();
                });
        String key = hasDyes ? "item.jojo.cassette_blank.hint.has_dyes" : "item.jojo.cassette_blank.hint";
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }

}
