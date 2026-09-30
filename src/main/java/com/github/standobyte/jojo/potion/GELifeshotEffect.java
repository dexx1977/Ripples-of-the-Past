package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectCategory;

public class GELifeshotEffect extends StatusEffect implements IApplicableEffect {

    public GELifeshotEffect(int liquidColor) {
        super(MobEffectCategory.HARMFUL, liquidColor);
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; 
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;
            CommonReflection.setCreeperSwell(creeper, -1);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap modifiers, int amplifier) {
        super.addAttributeModifiers(entity, modifiers, amplifier);
        if (entity instanceof Mob) {
            ((Mob) entity).setNoAi(true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap modifiers, int amplifier) {
        super.removeAttributeModifiers(entity, modifiers, amplifier);
        if (entity instanceof Mob) {
            ((Mob) entity).setNoAi(false);
        }
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return !(entity instanceof Player && ((Player) entity).abilities.instabuild)
                && !(entity instanceof Mob && ((Mob) entity).isNoAi());
    }
}
