package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.block.StoneMaskBlock;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;

public class StoneMaskItem extends CustomModelArmorItem {
    public static final String NBT_ACTIVATION_KEY = "Activated";
    private final StoneMaskBlock block;
    private String textureActivatedStr;

    public StoneMaskItem(ArmorMaterial material, EquipmentSlot slot, Properties builder, StoneMaskBlock block) {
        super(material, slot, builder);
        this.block = block;
        registerBlocks(Item.BY_BLOCK, this);
    }

    public static void setActivatedArmorTexture(ItemStack stack) {
        stack.getTag().putByte(NBT_ACTIVATION_KEY, (byte) 101);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        CompoundTag tag = stack.getTag();
        byte textureTicks = tag.getByte(NBT_ACTIVATION_KEY);
        if (textureTicks > 0) {
            tag.putByte(NBT_ACTIVATION_KEY, --textureTicks);
            if (textureTicks == 0) {
                Iterable<ItemStack> armor = entity.getArmorSlots();
                if (armor instanceof List) {
                    List<ItemStack> armorList = ((List<ItemStack>) armor);
                    int index = EquipmentSlot.HEAD.getIndex();
                    if (armorList.get(index) == stack) {
                        armorList.set(index, ItemStack.EMPTY);
                        entity.spawnAtLocation(stack);
                    }
                }
            }
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        boolean activated = stack.getTag().getByte(NBT_ACTIVATION_KEY) > 0;
        if (activated) {
            if (textureActivatedStr == null) {
                ResourceLocation regName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this);
                textureActivatedStr = createTexturePath(new ResourceLocation(regName.getNamespace(), regName.getPath() + "_activated"));
            }
            return textureActivatedStr;
        }
        return super.getArmorTexture(stack, entity, slot, type);
    }

    ////////////////////////////////////////////////////
    // "no multiple inheritance" moment
    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult actionresulttype = this.place(new BlockPlaceContext(context));
        return !actionresulttype.consumesAction() && this.isEdible() ? this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult() : actionresulttype;
    }

    public InteractionResult place(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockPlaceContext blockitemusecontext = this.updatePlacementContext(context);
            if (blockitemusecontext == null) {
                return InteractionResult.FAIL;
            } else {
                BlockState blockstate = this.getPlacementState(blockitemusecontext);
                if (blockstate == null) {
                    return InteractionResult.FAIL;
                } else if (!this.placeBlock(blockitemusecontext, blockstate)) {
                    return InteractionResult.FAIL;
                } else {
                    BlockPos blockpos = blockitemusecontext.getClickedPos();
                    Level world = blockitemusecontext.getLevel();
                    Player playerentity = blockitemusecontext.getPlayer();
                    ItemStack itemstack = blockitemusecontext.getItemInHand();
                    BlockState blockstate1 = world.getBlockState(blockpos);
                    Block block = blockstate1.getBlock();
                    if (block == blockstate.getBlock()) {
                        blockstate1 = this.updateBlockStateFromTag(blockpos, world, itemstack, blockstate1);
                        this.updateCustomBlockEntityTag(blockpos, world, playerentity, itemstack, blockstate1);
                        block.setPlacedBy(world, blockpos, blockstate1, playerentity, itemstack);
                        if (playerentity instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)playerentity, blockpos, itemstack);
                        }
                    }

                    SoundType soundtype = blockstate1.getSoundType(world, blockpos, context.getPlayer());
                    world.playSound(playerentity, blockpos, this.getPlaceSound(blockstate1, world, blockpos, context.getPlayer()), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (playerentity == null || !playerentity.abilities.instabuild) {
                        itemstack.shrink(1);
                    }

                    return InteractionResult.sidedSuccess(world.isClientSide());
                }
            }
        }
    }

    protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }   

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        return context;
    }

    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        return updateCustomBlockEntityTag(world, player, pos, stack);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState blockstate = this.getBlock().getStateForPlacement(context);
        return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;
    }

    private BlockState updateBlockStateFromTag(BlockPos pos, Level world, ItemStack stack, BlockState state) {
        BlockState blockstate = state;
        CompoundTag compoundnbt = stack.getTag();
        if (compoundnbt != null) {
            CompoundTag compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> statecontainer = state.getBlock().getStateDefinition();

            for(String s : compoundnbt1.getAllKeys()) {
                Property<?> property = statecontainer.getProperty(s);
                if (property != null) {
                    String s1 = compoundnbt1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != state) {
            world.setBlock(pos, blockstate, 2);
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState state, Property<T> property, String string) {
        return property.getValue(string).map((p_219986_2_) -> {
            return state.setValue(property, p_219986_2_);
        }).orElse(state);
    }

    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player playerentity = context.getPlayer();
        CollisionContext iselectioncontext = playerentity == null ? CollisionContext.empty() : CollisionContext.of(playerentity);
        return (!this.mustSurvive() || state.canSurvive(context.getLevel(), context.getClickedPos())) && context.getLevel().isUnobstructed(state, context.getClickedPos(), iselectioncontext);
    }

    protected boolean mustSurvive() {
        return true;
    }

    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return context.getLevel().setBlock(context.getClickedPos(), state, 11);
    }

    public static boolean updateCustomBlockEntityTag(Level world, @Nullable Player player, BlockPos pos, ItemStack stack) {
        MinecraftServer minecraftserver = world.getServer();
        if (minecraftserver == null) {
            return false;
        } else {
            CompoundTag compoundnbt = stack.getTagElement("BlockEntityTag");
            if (compoundnbt != null) {
                BlockEntity tileentity = world.getBlockEntity(pos);
                if (tileentity != null) {
                    if (!world.isClientSide() && tileentity.onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) {
                        return false;
                    }

                    CompoundTag compoundnbt1 = tileentity.saveWithoutMetadata();
                    CompoundTag compoundnbt2 = compoundnbt1.copy();
                    compoundnbt1.merge(compoundnbt);
                    if (!compoundnbt1.equals(compoundnbt2)) {
                        tileentity.load(compoundnbt1);
                        tileentity.setChanged();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @Override
    public String getDescriptionId() {
        return this.getBlock().getDescriptionId();
    }

    /** The mask block the mod's tab showed next to the mask item. */
    public void addToCreativeTab(CreativeModeTab.Output output) {
        Block block = this.getBlock();
        if (block != null) {
            // 1.20.1 rejects stacks whose count is not 1, and a block that has no item
            // of its own yields an empty stack, so the mask item is used in that case
            // (in 1.16.5 the block stack was the same mask item anyway)
            Item blockItem = block.asItem();
            output.accept(blockItem == Items.AIR ? new ItemStack(this) : new ItemStack(blockItem));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> text, TooltipFlag flag) {
        super.appendHoverText(stack, world, text, flag);
        this.getBlock().appendHoverText(stack, world, text, flag);
    }

    public Block getBlock() {
        // the 1.16.5 code unwrapped a RegistryObject here; the field is a plain block reference now
        return this.getBlockRaw();
    }

    private Block getBlockRaw() {
        return this.block;
    }

    public void registerBlocks(Map<Block, Item> map, Item item) {
        map.put(this.getBlock(), item);
    }

    public void removeFromBlockToItemMap(Map<Block, Item> map, Item item) {
        map.remove(this.getBlock());
    }
}