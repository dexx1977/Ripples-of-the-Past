package com.github.standobyte.jojo.item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.cassette.CassetteCap;
import com.github.standobyte.jojo.item.cassette.CassetteCap.TrackSourceList;
import com.github.standobyte.jojo.item.cassette.TrackSourceDye;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

public class CassetteRecordedItem extends Item {

    public CassetteRecordedItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        CassetteCap cassette = CassetteRecordedItem.getCassetteData(stack).orElse(null);
        TrackSourceList trackSources = cassette != null ? cassette.getTracks() : TrackSourceList.BROKEN_CASSETTE;
        if (trackSources.isBroken()) {
            tooltip.add(Component.translatable("jojo.cassette.bad_recording")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        else {
            WalkmanSoundHandler.CassetteTracksSided.fromSourceList(trackSources).forEach((side, tracks) -> {
                if (!tracks.isEmpty()) {
                    tooltip.add(Component.translatable("jojo.cassette." + side.name().toLowerCase())
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    tracks.forEach(track -> tooltip.add(track.getName().withStyle(ChatFormatting.GRAY)));
                    tooltip.add(Component.literal(" "));
                }
            });
            
            if (cassette.hasDyeCraftHint()) {
                DyeColor dye = cassette.getDye();
                if (dye != null) {
                    Item dyeItem = DyeItem.byColor(dye);
                    tooltip.add(Component.translatable("item.jojo.cassette.dye_hint", 
                            Component.translatable(ModItems.CASSETTE_BLANK.get().getDescriptionId()),
                            Component.translatable(dyeItem.getDescriptionId()))
                            .withStyle(ChatFormatting.GRAY));
                }
            }
            
            int generation = cassette.getGeneration();
            tooltip.add(Component.translatable("jojo.cassette.generation." + Math.min(generation, 2))
                    .withStyle(ChatFormatting.GRAY));
        }
        
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }
    
    /** The cassettes the mod's tab showed, one per dye that has tracks. */
    public void addToCreativeTab(CreativeModeTab.Output output) {
        {
//            boolean isClientSide = net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT; // nope
            boolean isClientSide = true;
            if (isClientSide) {
                for (DyeColor dye : DyeColor.values()) {
                    TrackSourceDye source = new TrackSourceDye(dye);
                    if (WalkmanSoundHandler.CassetteTracksSided.getTracks(source)
                            .findAny().isPresent()) {
                        ItemStack cassette = new ItemStack(this);
                        CassetteRecordedItem.editCassetteData(cassette, cap -> {
                            cap.setDye(dye);
                            cap.addDyeCraftHint();
                            cap.recordTracks(Collections.singletonList(source));
                        });
                        output.accept(cassette);
                    }
                }
            }
        }
    }
    
    
    public static Optional<CassetteCap> getCassetteData(ItemStack recordedCassetteItem) {
        if (recordedCassetteItem.isEmpty()) return Optional.empty();
        return MCUtil.nbtGetCompoundOptional(recordedCassetteItem.getOrCreateTag(), "Cassette")
                .map(nbt -> {
                    CassetteCap data = new CassetteCap(recordedCassetteItem);
                    data.fromNBT(nbt);
                    return data;
                });
    }
    
    public static void editCassetteData(ItemStack recordedCassetteItem, Consumer<CassetteCap> action) {
        CassetteCap cassetteData = getCassetteData(recordedCassetteItem).orElse(new CassetteCap(recordedCassetteItem));
        action.accept(cassetteData);
        recordedCassetteItem.getOrCreateTag().put("Cassette", cassetteData.toNBT());
    }
    
}
