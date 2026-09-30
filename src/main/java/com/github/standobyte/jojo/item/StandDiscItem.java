package com.github.standobyte.jojo.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.StoryPart;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IConfigurable;

public class StandDiscItem extends Item {
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.github.standobyte.jojo.client.ClientItemRenderers.initialize(this, consumer);
    }

    private static final String STAND_TAG = "Stand";
    public static final String WS_TAG = "WSPutOut";

    public StandDiscItem(Properties properties) {
        super(properties);

        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                if (validStandDisc(stack, false)) {
                    StandInstance stand = getStandFromStack(stack, false);
                    if (MCUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                        return IStandPower.getStandPowerOptional(entity).map(power -> {
                            return giveStandFromDisc(power, stand, stack);
                        }).orElse(false);
                    }, true)) {
                        return stack;
                    }
                }
                return super.execute(blockSource, stack);
            }
        });
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (validStandDisc(stack, false)) {
                StandInstance stand = getStandFromStack(stack, false);
                if (JojoModConfig.getCommonConfigInstance(false).isStandBanned(stand.getType())) {
                    return InteractionResultHolder.fail(stack);
                }
                
                if (!player.abilities.instabuild) {
                    Optional<StandInstance> previousDiscStand = power.putOutStand();
                    previousDiscStand.ifPresent(prevStand -> {
                        ItemEntity discItemEntity = player.drop(withStand(new ItemStack(this), prevStand), false);
                        discItemEntity.setPickUpDelay(5);
                        discItemEntity.setThrower(player.getUUID());
                    });
                }
                else {
                    power.clear();
                }
                
                if (giveStandFromDisc(power, stand, stack)) {
                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResultHolder.success(stack);
                }
                else {
                    return InteractionResultHolder.fail(stack);
                }
            } 
        }
        else if (!power.hasPower()) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    
    /** The discs the mod's tab showed, one per stand that is available. */
    public void addToCreativeTab(CreativeModeTab.Output output) {
        {
            boolean isClientSide = net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT;
            List<StandType<?>> legalStands = new ArrayList<>();
            for (StandType<?> standType : JojoCustomRegistries.STANDS.getRegistry()) {
                if (standType.getSurvivalGameplayPool().addToCreativeTab(this, standType, isClientSide)) {
                    legalStands.add(standType);
                }
            }
            
            legalStands.stream()
            .sorted(Comparator.comparing(StandType::getPartName, StoryPart.partNamesComparator()))
            .forEach(stand -> output.accept(withStand(new ItemStack(this), new StandInstance(stand))));
        }
    }
    
    @Override
    public boolean allowdedIn(CreativeModeTab creativeTab) {
        return super.allowdedIn(creativeTab);
    }
    
    
    public static ItemStack withStand(ItemStack discStack, StandInstance standInstance) {
        discStack.getOrCreateTag().put(STAND_TAG, standInstance.writeNBT());
        return discStack;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        Player player = ClientUtil.getClientPlayer();
        if (player != null) {
            if (validStandDisc(stack, true)) {
                StandInstance stand = getStandFromStack(stack, true);
                tooltip.add(stand.getName());
                Component partName = StandSkinsManager.getInstance()
                        .getStandSkin(stand.getSelectedSkin())
                        .map(skin -> skin.getPartName(stand.getType()))
                        .orElse(stand.getType().getPartName());
                tooltip.add(partName);
                for (StandPart standPart : StandPart.values()) {
                    if (!stand.hasPart(standPart)) {
                        tooltip.add(Component.translatable("jojo.disc.missing_part." + standPart.name().toLowerCase()).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
        }
        
        String modId = getCreatorModId(stack);
        if (!modId.equals(JojoMod.MOD_ID)) {
            ModList.get().getModContainerById(modId)
            .map(mod -> mod.getModInfo())
            .flatMap(modInfo -> modInfo instanceof IConfigurable ? ((IConfigurable) modInfo).getConfigElement("authors") : Optional.empty())
            .map(authorsString -> authorsString instanceof String ? (String) authorsString : null)
            .ifPresent(authors -> {
                authors = authors.replace(", StandoByte", "").replace("StandoByte, ", "");
                tooltip.add(Component.translatable("item.jojo.stand_disc.addon_author", authors)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            });
        }
        
        tooltip.add(Component.translatable("item.jojo.creative_only_tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
    
    @Deprecated
    public static StandInstance getStandFromStack(ItemStack stack, boolean clientSide) {
        return getStandFromStack(stack);
    }
    
    @Nullable
    public static StandInstance getStandFromStack(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(STAND_TAG, MCUtil.getNbtId(CompoundTag.class))) {
            return null;
        }
        return StandInstance.fromNBT((CompoundTag) nbt.get(STAND_TAG));
    }
    
    public static boolean validStandDisc(ItemStack stack, boolean clientSide) {
        StandInstance stand = getStandFromStack(stack, clientSide);
        return stand != null && stand.getType() != null;
    }

    public static int getColor(ItemStack itemStack) {
        if (!validStandDisc(itemStack, true)) {
            return 0xFFFFFF;
        } else {
            return StandSkinsManager.getUiColor(getStandFromStack(itemStack, true));
        }
    }
    
    @Override
    public String getCreatorModId(ItemStack itemStack) {
        ResourceLocation id;
        StandInstance stand = getStandFromStack(itemStack);
        if (stand != null) {
            id = stand.getType().getRegistryName();
        }
        else {
            id = MCUtil.id(this);
        }
        return id.getNamespace();
    }
    
    
    
    public static boolean giveStandFromDisc(IStandPower standCap, StandInstance stand, ItemStack discItem) {
        boolean standExistedBefore = discItem.getTag().getBoolean(WS_TAG);
        return standCap.giveStandFromInstance(stand, standExistedBefore);
    }
}