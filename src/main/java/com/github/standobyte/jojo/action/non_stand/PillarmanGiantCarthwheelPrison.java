package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.Level;

public class PillarmanGiantCarthwheelPrison extends PillarmanAction {

    public PillarmanGiantCarthwheelPrison(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.HEAT;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RED.get(), 6);
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int n = 8;
            for (int i = 0; i < n; i++) {
                Vec2 rotOffsets2 = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 2);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y, 0, -0.5D, 0);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y + 180, 0, -0.5D, 0);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y + 90, 0, -0.5D, 0);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y - 90, 0, -0.5D, 0);
            }
            user.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, false));
        }
    }
    
    @Override
    public boolean clHeldStartAnim(Player user) {
        return ModPlayerAnimations.giantCartwheelPrison.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.giantCartwheelPrison.setAnimEnabled(user, false);
    }
    
}
