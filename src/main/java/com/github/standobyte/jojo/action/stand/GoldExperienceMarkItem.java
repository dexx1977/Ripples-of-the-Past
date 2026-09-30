package com.github.standobyte.jojo.action.stand;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.rendertype.CustomRenderType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class GoldExperienceMarkItem extends StandAction {

    public GoldExperienceMarkItem(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack item = user.getItemInHand(InteractionHand.OFF_HAND);
        if (item.isEmpty()) {
            return conditionMessage("ge_lifeform_material_only_item");
        }
        if (!GoldExperienceCreateLifeform.canGiveLifeTo(item)) {
            return conditionMessage("ge_lifeform_material_item");
        }
        
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(Level world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable FriendlyByteBuf extraInput) {
        if (!world.isClientSide()) {
            ItemStack heldItem = user.getItemInHand(InteractionHand.OFF_HAND);
            if (!heldItem.isEmpty() && user instanceof ServerPlayer) {
                ItemStack markedStack;
                boolean give = false;
                if (heldItem.getCount() == 1) {
                    markedStack = heldItem;
                    ItemStack mainHandItem = user.getItemInHand(InteractionHand.MAIN_HAND);
                    // keep the bow/crossbow in main hand
                    if (!mainHandItem.isEmpty()
                            /*&& mainHandItem.getItem() instanceof ShootableItem && ((ShootableItem) mainHandItem.getItem()).getAllSupportedProjectiles().test(heldItem)*/) {
                        user.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                        give = true;
                    }
                    // swap items
                    else {
                        user.setItemInHand(InteractionHand.OFF_HAND, mainHandItem);
                        user.setItemInHand(InteractionHand.MAIN_HAND, heldItem);
                    }
                }
                else {
                    markedStack = heldItem.split(1);
                    if (user.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                        user.setItemInHand(InteractionHand.MAIN_HAND, markedStack);
                    }
                    else {
                        give = true;
                    }
                }
                user.stopUsingItem();

                StandEffectsTracker standEffects = power.getContinuousEffects();
                standEffects.getEffects()
                .filter(effect -> effect.effectType == ModStandEffects.GE_ITEM_MARK.get())
                .forEach(standEffects::removeEffect);
                
                TrackerItemStack itemTracker = TrackerItemStack.setTracked(markedStack, (ServerPlayer) user);
                if (itemTracker != null) {
                    itemTracker.setAtEntity(user.getId(), world, KnownItemState.ENTITY_HAS_ITEM);
                    
                    GEItemMarkEffect effect = new GEItemMarkEffect(itemTracker.getTrackerId());
                    effect.withStand(power);
                    standEffects.addEffect(effect);
                    
                    MCUtil.playSound(world, null, user, ModSounds.GOLD_EXPERIENCE_LIFE_ITEM.get(), 
                            user.getSoundSource(), 0.5f, 1.0f, StandUtil::playerCanHearStands);
                }
                
                // needs to be done after the tracker NBT has been set
                if (give) {
                    MCUtil.giveItemTo(user, markedStack, true);
                }
            }
        }
    }
    

    
    public static Optional<TrackerItemStack> getTargetedMarkedItem(IStandPower power, LivingEntity player) {
        return getTargetedEffect(getTargets(power, player), player).map(effect -> effect.getItemTracker(false));
    }
    
    public static Optional<GEItemMarkEffect> getTargetedEffect(List<Pair<GEItemMarkEffect, Vec3>> targets, LivingEntity player) {
        // TODO return empty on specific conditions (holding a fitting item off-hand)
        
        Vec3 lookAngle = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition(1.0F);
        Optional<GEItemMarkEffect> outlined = targets.stream()
                .map(e -> Pair.of(e, lookAngle.dot(e.getRight().subtract(eyePos).normalize())))
                .filter(withCos -> withCos.getValue() > 0.92388)
                .max(Comparator.comparingDouble(Pair::getValue))
                .map(Pair::getLeft)
                .map(Pair::getLeft);
        
        return outlined;
    }
    
    public static List<Pair<GEItemMarkEffect, Vec3>> getTargets(IStandPower stand, LivingEntity player) {
        double range = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance;
        float partialTick = player.level.isClientSide() ? ClientUtil.getPartialTick() : 1;
        List<Pair<GEItemMarkEffect, Vec3>> targets = stand.getContinuousEffects()
                .getEffects()
                .filter(effect -> effect.effectType == ModStandEffects.GE_ITEM_MARK.get())
                .map(effect -> (GEItemMarkEffect) effect)
                .filter(effect -> effect.getItemTracker(true) != null && effect.getItemTracker(false).getAtEntity(player.level) != player)
                .map(effect -> Pair.of(effect, effect.getItemTracker(false).markerPos(player.level, partialTick)))
                .filter(entry -> entry.getRight() != null && entry.getRight().distanceToSqr(player.position()) < range * range)
                .collect(Collectors.toList());
        return targets.size() > 1 ? Collections.singletonList(targets.get(targets.size() - 1)) : targets;
    }
    
    @Override
    public MutableComponent getTranslatedName(IStandPower power, String key) {
        ItemStack item = power.getUser().getOffhandItem();
        if (!item.isEmpty() && GoldExperienceCreateLifeform.canGiveLifeTo(item)) {
            return Component.translatable(key + ".param", item.getDisplayName());
        }
        else {
            return super.getTranslatedName(power, key);
        }
    }
    
    
    public static class ClientStuff {
        
        public static VertexConsumer qwe(VertexConsumer vertexBuilder, ItemStack item, boolean direct, 
                MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Entry pMatrixEntry) {
            Player player = Minecraft.getInstance().player;
            if (player != null && GEItemMarkEffect.isItemMarked(item, player)) {
                if (item.getItem() == Items.COMPASS) {
                    if (direct) {
                        vertexBuilder = getCompassFoilBufferDirect(pBuffer, pRenderType, pMatrixEntry);
                    } else {
                        vertexBuilder = getCompassFoilBuffer(pBuffer, pRenderType, pMatrixEntry);
                    }
                }
                else if (direct) {
                    vertexBuilder = getFoilBufferDirect(pBuffer, pRenderType);
                } else {
                    vertexBuilder = getFoilBuffer(pBuffer, pRenderType);
                }
            }
            return vertexBuilder;
        }
        
        public static VertexConsumer getCompassFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Entry pMatrixEntry) {
            return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(
                    pBuffer.getBuffer(CustomRenderType.geImbuedGlint()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
        }

        public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Entry pMatrixEntry) {
            return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(
                    pBuffer.getBuffer(CustomRenderType.geImbuedGlintDirect()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
        }

        public static VertexConsumer getFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType) {
            if (Minecraft.useShaderTransparency() && pRenderType == Sheets.translucentItemSheet()) {
                return VertexBuilderUtils.create(pBuffer.getBuffer(CustomRenderType.geImbuedGlintTranslucent()), pBuffer.getBuffer(pRenderType));
            }
            else {
                return VertexBuilderUtils.create(pBuffer.getBuffer(CustomRenderType.geImbuedGlint()), pBuffer.getBuffer(pRenderType));
            }
        }

        public static VertexConsumer getFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType) {
            return VertexBuilderUtils.create(pBuffer.getBuffer(CustomRenderType.geImbuedGlintDirect()), pBuffer.getBuffer(pRenderType));
        }
    }
    
}
