package com.github.standobyte.jojo.entity.mob;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class HamonMasterEntity extends Mob implements Npc, IMobPowerUser, IEntityAdditionalSpawnData {
    private final INonStandPower hamonPower = new NonStandPower(this);
    @Deprecated
    private boolean reAddBaseHamon; // FIXME remove in v0.2.3
    
    public HamonMasterEntity(EntityType<? extends HamonMasterEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public boolean removeWhenFarAway(double distanceFromPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            getPower().getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                HamonUtil.interactWithHamonTeacher(level, player, this, hamon);
            });
        }
        return super.mobInteract(player, hand);
    }
    
    @Override
    public INonStandPower getPower() {
        return hamonPower;
    }
    
    @Override
    public void tick() {
        super.tick();
        getPower().tick();
        if (level.isClientSide() && tickCount % 20 == 0) {
            if (!fluidHeight.isEmpty()) {
                for (Map.Entry<TagKey<Fluid>, Double> entry : fluidHeight.object2DoubleEntrySet()) {
                    if (entry.getValue() > 0 && entry.getValue() < 0.4) {
                        // FIXME hamon master liquid walking sfx
                        HamonUtil.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), position(), 0.1F);
                        break;
                    }
                }
            }
        }
        getPower().postTick();
    }

    // FIXME hamon master liquid walking
    @Override
    public boolean canStandOnFluid(net.minecraft.world.level.material.FluidState fluid) {
        return hamonPower.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> 
        hamon.isSkillLearned(ModHamonSkills.LIQUID_WALKING.get()))
                .orElse(false);
    }

    @Override
    public void lavaHurt() {} // Entity#lavaHurt is public in 1.20.1
    
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, 
            @Nullable SpawnGroupData additionalData, @Nullable CompoundTag nbt) {
        addMasterHamon(hamonPower);
        setPersistenceRequired();
        
        return super.finalizeSpawn(world, difficulty, reason, additionalData, nbt);
    }
    
    @Deprecated
    private void restoreHamon() {
        if (reAddBaseHamon && !level.isClientSide()) {
            addMasterHamon(hamonPower);
            reAddBaseHamon = false;
        }
    }
    
    @Override
    public boolean isInvulnerableTo(DamageSource pDamageSource) {
        return super.isInvulnerableTo(pDamageSource) || pDamageSource.getMsgId().startsWith("hamon");
    }
    
    public void addMasterHamon(INonStandPower power) {
        hamonPower.givePower(ModPowers.HAMON.get());
        HamonData hamon = hamonPower.getTypeSpecificData(ModPowers.HAMON.get()).get();
        hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
        hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
        hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
        for (AbstractHamonSkill skill : JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues()) {
            if (skill.isBaseSkill()) {
                hamon.addHamonSkill(this, skill, false, false);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("HamonPower", hamonPower.writeNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HamonPower", MCUtil.getNbtId(CompoundTag.class))) {
            hamonPower.readNBT(nbt.getCompound("HamonPower"));
        }
        reAddBaseHamon = true;
    }

    @Deprecated
    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        restoreHamon();
    }

    @Deprecated
    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {}

    @Override
    protected void registerGoals() {} // TODO Hamon Master ai
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_SPEED, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(ForgeMod.SWIM_SPEED.get(), 2.0);
    }
}
