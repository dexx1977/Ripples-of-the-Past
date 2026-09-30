package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class UndeadRegenerationEffect extends MobEffect implements IApplicableEffect {

    public UndeadRegenerationEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int k = 50 >> amplifier;
        if (k > 0) {
            return duration % k == 0;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return !(entity instanceof Player && JojoModUtil.isPlayerJojoVampiric((Player) entity)) && entity.getMobType() == MobType.UNDEAD;
    }
}
