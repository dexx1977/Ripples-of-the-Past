package com.github.standobyte.jojo.power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.power.bowcharge.IBowChargeEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import com.github.standobyte.jojo.init.power.RegistryEntry;

public interface IPowerType<P extends IPower<P, T>, T extends IPowerType<P, T>> extends RegistryEntry<T> {
    static final String NO_POWER_NAME = "";
    boolean isReplaceableWith(T newType);
    boolean keepOnDeath(P power);
    
    void tickUser(LivingEntity entity, P power);
    default void postTickUser(LivingEntity entity, P power) {}
    default void onNewDay(LivingEntity user, P power, long prevDay, long day) {}
    default HitResult clientHitResult(P power, Entity cameraEntity, HitResult vanillaHitResult) {
        return vanillaHitResult;
    }
    
    ControlScheme.DefaultControls clCreateDefaultLayout();
    void clAddMissingActions(ControlScheme controlScheme, P power);
    default boolean isActionLegalInHud(Action<P> action, P power) { return true; }
    
    @Nullable default IBowChargeEffect<P, T> getBowChargeEffect() {
        return null;
    }
    
    float getTargetResolveMultiplier(P power, IStandPower attackingStand);
    
    String getTranslationKey();
    default MutableComponent getName() {
        return Component.translatable(getTranslationKey());
    }
    
    ResourceLocation getIconTexture(@Nullable P power);
}
