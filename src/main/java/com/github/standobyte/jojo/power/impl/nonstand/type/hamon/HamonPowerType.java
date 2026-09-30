package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRunAwayPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterTechniqueHamonSkill;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public class HamonPowerType extends NonStandPowerType<HamonData> {
    public static final int COLOR = 0xFFFF00;

    public HamonPowerType(HamonAction[] startingAttacks, HamonAction[] startingAbilities, HamonAction defaultMmb) {
        super(startingAttacks, startingAbilities, defaultMmb, HamonData::new);
    }

    public HamonPowerType(HamonAction[] startingAttacks, HamonAction[] startingAbilities) {
        super(startingAttacks, startingAbilities, startingAbilities[0], HamonData::new);
    }
    
    @Override
    public boolean keepOnDeath(INonStandPower power) {
        return JojoModConfig.getCommonConfigInstance(false).keepHamonOnDeath.get();
    }
    
    
    @Override
    public void clAddMissingActions(ControlScheme controlScheme, INonStandPower power) {
        super.clAddMissingActions(controlScheme, power);
        
        HamonData hamon = power.getTypeSpecificData(this).get();
        CharacterHamonTechnique technique = hamon.getCharacterTechnique();
        Collection<CharacterTechniqueHamonSkill> techniqueSkills = hamon.getTechniqueData().getLearnedSkills();
        
        if (technique != null) {
            technique.getPerksOnPick().forEach(techniquePerk -> {
                addSkillAction(techniquePerk, controlScheme);
            });
        }
        for (AbstractHamonSkill techniqueSkill : techniqueSkills) {
            addSkillAction(techniqueSkill, controlScheme);
        }
    }
    
    private static void addSkillAction(AbstractHamonSkill skill, ControlScheme controlScheme) {
        skill.getRewardActions(true).forEach(action -> {
            ControlScheme.Hotbar hotbar;
            switch (skill.getRewardType()) {
            case ATTACK:
                hotbar = ControlScheme.Hotbar.LEFT_CLICK;
                break;
            default:
                hotbar = ControlScheme.Hotbar.RIGHT_CLICK;
                break;
            }
            controlScheme.addIfMissing(hotbar, action);
        });
    }
    
    @Override
    public boolean isActionLegalInHud(Action<INonStandPower> action, INonStandPower power) {
        if (action instanceof HamonAction) {
            AbstractHamonSkill hamonSkill = ((HamonAction) action).getUnlockingSkill();
            if (hamonSkill != null && hamonSkill.addsExtraToHud()) {
                HamonData hamon = power.getTypeSpecificData(this).get();
                Collection<CharacterTechniqueHamonSkill> techniqueSkills = hamon.getTechniqueData().getLearnedSkills();
                return techniqueSkills.contains(hamonSkill);
            }
        }
        return super.isActionLegalInHud(action, power);
    }
    
    
    @Override
    public void onClear(INonStandPower power) {
        super.onClear(power);
        power.getTypeSpecificData(this).ifPresent(HamonData::onClear);
    }

    @Override
    public float getTargetResolveMultiplier(INonStandPower power, IStandPower attackingStand) {
        return 2;
    }

    @Override
    public boolean hasEnergy(INonStandPower power, float amount) {
        return amount == 0 || power.getEnergy() > 0 || power.getTypeSpecificData(this).get().getBreathStability() > 0;
    }

    @Override
    public boolean consumeEnergy(INonStandPower power, float amount) {
        return power.getTypeSpecificData(this).get().getHamonEnergyUsageEfficiency(amount, true) > 0;
    }
    
    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return power.getTypeSpecificData(this).get().isSkillLearned(ModHamonSkills.JUMP.get());
    }
    
    @Override
    public void onLeap(INonStandPower power) {
        power.getTypeSpecificData(this).get().hamonPointsFromAction(HamonStat.CONTROL, getLeapEnergyCost());
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        HamonData hamon = power.getTypeSpecificData(this).get();
        return hamon.isSkillLearned(ModHamonSkills.AFTERIMAGES.get()) ? 1.5F : 1.4F;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 20;
    }
    
    @Override
    public float getLeapEnergyCost() {
        return 250;
    }
    
    @Override
    public float getMaxEnergy(INonStandPower power) {
        return power.getTypeSpecificData(this).get().getMaxEnergy();
    }

    @Override
    public float tickEnergy(INonStandPower power) {
        return power.getTypeSpecificData(this).get().tickEnergy();
    }

    @Override
    public float getStaminaRegenFactor(INonStandPower power, IStandPower standPower) {
        return 1F + power.getTypeSpecificData(this).get().getBreathingLevel() * 0.01F;
    }

    @Override
    public void tickUser(LivingEntity user, INonStandPower power) {
        super.tickUser(user, power);
        HamonData hamon = power.getTypeSpecificData(this).get();
        Level world = user.level;
        hamon.tick();
        if (!world.isClientSide()) {
            if (user instanceof Player) {
                ServerPlayer player = (ServerPlayer) user;
                if (hamon.isSkillLearned(ModHamonSkills.ROPE_TRAP.get())) {
                    if (player.onGround() && player.isShiftKeyDown()) {
                        BlockPos pos = player.blockPosition();
                        if (player.level.isEmptyBlock(pos)) {
                            Inventory inventory = player.inventory;
                            for (int i = 8; i >= 0; i--) {
                                ItemStack stack = inventory.items.get(i);
                                if (!stack.isEmpty() && stack.getItem() == Items.STRING) {
                                    BlockPlaceContext ctx = new BlockPlaceContext(player, InteractionHand.OFF_HAND, stack, 
                                            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                                    BlockState state = Blocks.TRIPWIRE.getStateForPlacement(ctx);
                                    if (state != null && !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(
                                            world.dimension(), world, pos.below()), Direction.UP) && world.setBlock(pos, state, 3) && !player.abilities.instabuild) {
                                        stack.shrink(1);
                                    }
                                    break;
                                }
                            }
                        }
                    }
//                    if (player.fishing != null) {
//                        ItemStack mainHandItem = player.getMainHandItem();
//                        if (mainHandItem.getItem() instanceof FishingRodItem) {
//                            Entity hooked = player.fishing.getHookedIn();
//                            if (hooked != null) {
//                                float energyCost = 30;
//                                if (power.consumeEnergy(energyCost)) {
//                                    ModDamageSources.dealHamonDamage(hooked, 0.0125F, player.fishing, player);
//                                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, energyCost);
//                                }
//                                else {
//                                    player.fishing.retrieve(mainHandItem);
//                                }
//                            }
//                        }
//                    }
                }
            }
        }
        
        if (!world.isClientSide() || user.is(ClientUtil.getClientPlayer())) {
            if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                if (user.isSprinting()) {
                    Vec3 vecBehind = Vec3.directionFromRotation(0, 180F + user.yRot).scale(8D);
                    AABB aabb = new AABB(user.position().subtract(0, 2D, 0), user.position().add(vecBehind.x, 2D, vecBehind.z));
                    List<LivingEntity> entitiesBehind = world.getEntitiesOfClass(LivingEntity.class, aabb, entity -> entity != user)
                            .stream().filter(entity -> !(entity instanceof StandEntity)).collect(Collectors.toList());
                    if (!entitiesBehind.isEmpty()) {
                        if (world.isClientSide()) {
                            // 1.20.1 removed LocalPlayer#sprintTime, not sprinting is the same check
                            if (user instanceof LocalPlayer && !user.isSprinting()) {
                                PacketManager.sendToServer(new ClRunAwayPacket());
                            }
                        }
                        else {
                            MobEffectInstance speed = user.getEffect(MobEffects.MOVEMENT_SPEED);
                            int speedAmplifier = speed != null && speed.getDuration() > 100 ? speed.getAmplifier() + 2 : 1;
                            user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, speedAmplifier, false, false, true));
                        }
                    }
                }
            }
            if (user instanceof Player) {
                hamon.tickExercises((Player) user);
            }
        }
    }
    
    @Override
    public void postTickUser(LivingEntity entity, INonStandPower power) {
        HamonData hamon = power.getTypeSpecificData(this).get();
        hamon.postTickWaterWalking(entity);
    }
    
    @Override
    public void onNewDay(LivingEntity user, INonStandPower power, long prevDay, long day) {
        if (user instanceof Player) {
            HamonData hamon = power.getTypeSpecificData(this).get();
            hamon.breathingTrainingDay((Player) user);
        }
    }

    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return newType == ModPowers.VAMPIRISM.get();
    }
}
