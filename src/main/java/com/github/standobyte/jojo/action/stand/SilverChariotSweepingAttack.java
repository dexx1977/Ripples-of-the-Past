package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.SCFlameSwingEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotSweepingAttack extends StandEntityHeavyAttack {

    public SilverChariotSweepingAttack(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity && !((SilverChariotEntity) stand).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return Math.max(super.getStandWindupTicks(standPower, standEntity) - getStandActionTicks(standPower, standEntity) / 2, 1);
    }
    
    @Override
    public void standTickWindup(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) {
            if (task.getTicksLeft() == 1) {
                SoundEvent sound = getPunchSwingSound();
                if (sound != null) {
                    standEntity.playSound(sound, 1.0F, 1.0F, ClientUtil.getClientPlayer());
                }
                
                double d0 = -Mth.sin(standEntity.yRot * MathUtil.DEG_TO_RAD);
                double d1 = Mth.cos(standEntity.yRot * MathUtil.DEG_TO_RAD);
                world.addParticle(ParticleTypes.SWEEP_ATTACK, standEntity.getX() + d0, standEntity.getY(0.5), standEntity.getZ() + d1, 
                        d0, 0.0D, d1);
            }
        }
    }
    
    @Override
    public void standPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            double reach = standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
            world.getEntities(standEntity, standEntity.getBoundingBox().inflate(reach, 0, reach), 
                    e -> !e.isSpectator() && e.isPickable() && standEntity.canHarm(e)).forEach(targetEntity -> {
                        Vec3 standLookVec = standEntity.getLookAngle();
                        Vec3 targetVec = targetEntity.position().subtract(standEntity.position()).normalize();
                        double cos = standLookVec.dot(targetVec);
                        if (cos > -0.5) {
                            StandEntityPunch slash = punchEntity(standEntity, targetEntity, standEntity.getDamageSource());
                            if (cos < 0.5) {
                                slash.damage(slash.getDamage() * 0.5F);
                            }
                            slash.doHit(task);
                        }
                    });
        }
    }
    
    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .impactSound(null)
                .addKnockback(1);
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state, Direction face) {
        return new StandBlockPunch(stand, pos, state, face);
    }
    
    @Override
    public void standTickPerform(Level world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && task.getTick() == 0
                && standEntity instanceof SilverChariotEntity) {
            SilverChariotEntity chariot = (SilverChariotEntity) standEntity;
            if (chariot.isRapierOnFire()) {
                SCFlameSwingEntity flame = new SCFlameSwingEntity(standEntity, world);
                flame.shootFromRotation(standEntity, 1.5F, 0.0F);
                standEntity.addProjectile(flame);
                chariot.removeRapierFire();
            }
        }
    }
}
