package com.github.standobyte.jojo.action.stand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class GoldExperienceRevertLifeform extends StandAction {

    public GoldExperienceRevertLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public Action<IStandPower> getVisibleAction(IStandPower power, ActionTarget target) {
        Action<IStandPower> action = super.getVisibleAction(power, target);
        if (action == this && 
                !StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance)
                .findAny().isPresent()) {
            action = null;
        }
        return action;
    }
    
    @Override
    public void clWriteExtraData(FriendlyByteBuf buf) {
        double distance = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance;
        clWriteTargetedStandEffect(buf, ModStandEffects.GE_CREATED_LIFEFORM.get(), distance);
    }
    
    @Override
    public void perform(Level world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable FriendlyByteBuf extraInput) {
        if (!world.isClientSide() && extraInput != null) {
            readTargetedStandEffect(extraInput, power, ModStandEffects.GE_CREATED_LIFEFORM.get())
            .ifPresent(effect -> {
                effect.remove();
                Entity entity = effect.getTarget();
                if (entity != null && entity.getType() == ModEntityTypes.GE_LIFEFORM_TRANSFORMATION.get()) {
                    power.setCooldownTimer(ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get(), 
                            power.getCooldownTimer(ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get()) - ((GETransformationEntity) entity).actionCooldown);
                }
            });
        }
    }
    
    
    @Override
    public MutableComponent getTranslatedName(IStandPower power, String key) {
        double distance = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance;
        Optional<StandEffectInstance> targetedEffect = clGetTargetedStandEffect(ModStandEffects.GE_CREATED_LIFEFORM.get(), distance);
        return targetedEffect.map(e -> {
            GECreatedLifeformEffect effect = (GECreatedLifeformEffect) e;
            return (MutableComponent) Component.translatable(key + ".param", effect.getName());
        }).orElse(super.getTranslatedName(power, key));
    }
    
    @Override
    public void renderActionIcon(PoseStack matrixStack, IStandPower power, float x, float y) {
        double distance = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance;
        ItemStack sourceItem = StandEffectsTracker.getTargetLookedAt((IStandPower) power, 
                ModStandEffects.GE_CREATED_LIFEFORM.get(), distance, ClientUtil.getClientPlayer())
                .map(effect -> ((GECreatedLifeformEffect) effect).getItemView())
                .orElse(ItemStack.EMPTY);
        if (!sourceItem.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(sourceItem, (int) x, (int) y);
        }
        else {
            super.renderActionIcon(matrixStack, power, x, y);
        }
    }
}
