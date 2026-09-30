package com.github.standobyte.jojo.entity.mob;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class StandUserDummyEntity extends Mob implements IMobStandUser, IEntityAdditionalSpawnData {
    private IStandPower stand = new StandPower(this);
    private StandAction action;
    private boolean useAction = false;
    
    public StandUserDummyEntity(Level world) {
        super(ModEntityTypes.STAND_USER_DUMMY.get(), world);
    }
    
    public StandUserDummyEntity(EntityType<? extends StandUserDummyEntity> type, Level world) {
        super(type, world);
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CommonReflection.getEntityCustomNameParameter().equals(key) && !level.isClientSide()) {
            updateStandAction();
        }
    }
    
    private void updateStandAction() {
        this.action = null;
        Component name = getCustomName();
        if (name != null) {
            String nameStr = name.getString();
            if (nameStr.contains(":")) {
                this.action = standActionFromId(new ResourceLocation(nameStr));
            }
            else if (stand.hasPower()) {
                ResourceLocation standId = stand.getType().getRegistryName();
                this.action = standActionFromId(new ResourceLocation(standId.getNamespace(), standId.getPath() + "_" + nameStr));
            }
        }
    }
    
    private static StandAction standActionFromId(ResourceLocation id) {
        if (JojoCustomRegistries.ACTIONS.getRegistry().containsKey(id)) {
            Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(id);
            if (action instanceof StandAction) {
                return (StandAction) action;
            }
        }
        return null;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && this.isAlive() && stand.hasPower() && action != null && useAction) {
            stand.clickAction(action, false, ActionTarget.EMPTY, null);
        }
        stand.tick();
        stand.postTick();
    }
    
    // TODO wear armor / hold items
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (item.getItem() == ModItems.STAND_DISC.get()) {
            if (!this.level.isClientSide) {
                Optional<StandInstance> previousDiscStand = stand.putOutStand();
                previousDiscStand.ifPresent(prevStand -> player.drop(StandDiscItem.withStand(
                        new ItemStack(ModItems.STAND_DISC.get()), prevStand), false));
                
                StandInstance discStand = StandDiscItem.getStandFromStack(item);
                if (StandDiscItem.giveStandFromDisc(stand, discStand, item)) {
                    stand.skipProgression();
                    if (!player.abilities.instabuild) {
                        item.shrink(1);
                    }
                    updateStandAction();
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        }
        else {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown()) {
                    useAction = !useAction;
                    if (!useAction) {
                        stand.stopHeldAction(false);
                    }
                }
                else {
                    stand.toggleSummon();
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }
    
    @Override
    public IStandPower getStandPower() {
        return stand;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("Stand", stand.writeNBT());
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Stand", MCUtil.getNbtId(CompoundTag.class))) {
            stand.readNBT(nbt.getCompound("Stand"));
            updateStandAction();
        }
    }
}
