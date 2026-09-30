package com.github.standobyte.jojo.action.non_stand;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class PillarmanSelfDetonation extends PillarmanAction {

    public PillarmanSelfDetonation(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.HEAT;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RED.get(), 12);
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.BOILING_BLOOD_POP.get(), 1);
        }
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide) {
            PillarmanExplosion explosion = new PillarmanExplosion(world, user, 
                    DamageSource.ON_FIRE.setExplosion(), null, 
                    user.getX(), user.getY(), user.getZ(), 3.0F, 
                    true, Explosion.BlockInteraction.DESTROY);
            CustomExplosion.explode(explosion);
            Player playerentity = user instanceof Player ? (Player)user : null;
            if (playerentity == null || !playerentity.abilities.instabuild) {
                user.hurt(EntityDamageSource.explosion(user), 40F);
                user.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
            }
        }
    }
    
    @Override
    public boolean clHeldStartAnim(Player user) {
        return ModPlayerAnimations.selfDetonation.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(Player user) {
        ModPlayerAnimations.selfDetonation.setAnimEnabled(user, false);
    } 
    
    
    public static class PillarmanExplosion extends CustomExplosion {

        public PillarmanExplosion(Level pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
            super(pLevel, pToBlowX, pToBlowY, pToBlowZ, pRadius);
        }

        public PillarmanExplosion(Level pLevel, @Nullable Entity pSource, 
                @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pDamageCalculator, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, boolean pFire, Explosion.BlockInteraction pBlockInteraction) {
            super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
        }
        
        @Override
        protected void filterEntities(List<Entity> entities) {
            LivingEntity acdc = getSourceMob();
            if (acdc != null) {
                Iterator<Entity> iter = entities.iterator();
                while (iter.hasNext()) {
                    Entity entity = iter.next();
                    if (entity == acdc || !MCUtil.canHarm(acdc, entity)) {
                        iter.remove();
                    }
                }
            }
        }
        
        @Override
        protected void spawnFire() {
            LivingEntity acdc = getSourceMob();
            if (acdc == null || ForgeEventFactory.getMobGriefingEvent(level, acdc)) {
                for (BlockPos pos : getToBlow()) {
                    if (level.isEmptyBlock(pos)) {
                        if (!level.isEmptyBlock(pos.below()) && Math.random() < 0.25f) {
                            level.setBlockAndUpdate(pos, ModBlocks.BOILING_BLOOD.get().defaultBlockState().setValue(LiquidBlock.LEVEL, 7));
                        } else {
                            level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                        }
                    }
                    else {
                        BlockState blockState = level.getBlockState(pos);
                        MRFlameEntity.meltIceAndSnow(level, blockState, pos);
                    }
                }
            }
        }
        
        @Override
        public ResourceLocation getExplosionType() {
            return CustomExplosion.Register.PILLAR_MAN_DETONATION;
        }
    }
    
}
