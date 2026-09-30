package com.github.standobyte.jojo.action.stand;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.ChooseLifeformScreen;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil.ResLocSet;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntityTypeToInstance;

import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class GoldExperienceChooseLifeform extends StandAction {
    
    public GoldExperienceChooseLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public boolean clientOnly() {
        ChooseLifeformScreen.openWindowOnClick();
        return false;
    }
    
    @Override
    public void onClick(Level world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide()) {
            user.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getMetMobs).ifPresent(
                    metMobs -> metMobs.updateNativeMobs((ServerLevel) world, user, true));
        }
    }
    
    @Override
    public void onProgressionSkipped(IStandPower power) {
        super.onProgressionSkipped(power);
        LivingEntity user = power.getUser();
        if (user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            GoldExperienceChooseLifeform.unlockAllEntityTypes(player);
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(data -> {
                data.metEntityTypes.syncToClient(player);
                PacketManager.sendToClient(data.getGELifeformsUIState().makePacket(), player);
            });
        }
    }
    

    public static void registerExtraEntitySubtypes() {
        EntitySubtype.registerSubtype(
                ModEntityTypes.COCO_JUMBO_TURTLE.get(), 
                "stand", 
                entity -> StandVirusEffect.getRandomStandGiver(entity).ifPresent(standGiver -> standGiver.giveStand(entity)), 
                entity -> entity.getStandPower().getType() == ModStandsInit.MR_PRESIDENT.get());
    }
    
    public static boolean isValidLifeform(EntitySubtype<?> entitySubtype, Level world) {
        Entity entity = EntityTypeToInstance.getEntityInstance(entitySubtype, world);
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            EntityType<?> entityType = entitySubtype.vanillaType;
            
            MobType mobType = mob.getMobType();
            if (
                    mobType == MobType.UNDEAD || 
                    mobType == MobType.ILLAGER ||
                    entityType == EntityType.TRADER_LLAMA ||
                    !entityType.canSummon()) {
                return false;
            }
            
//            if (world.getDifficulty() == Difficulty.PEACEFUL && mob.shouldDespawnInPeaceful()) {
//                return false;
//            }
            
            if (entityType == ModEntityTypes.COCO_JUMBO_TURTLE.get() && !IStandPower.getStandPowerOptional(mob).map(IStandPower::hasPower).orElse(false)) {
                return false;
            }
            
            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! tmp, will be reserved for specific items/blocks
            if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
                return false;
            }
            
            if (!(mob instanceof AmbientCreature || mob instanceof PathfinderMob
                    || mob instanceof FlyingMob || mob instanceof Slime)) {
                return false;
            }
            
            if (mob instanceof Npc || mob instanceof Merchant
                    || mob instanceof AbstractGolem || mob instanceof PatrollingMonster
                    || mob instanceof Ghast || mob instanceof Blaze || mob instanceof Vex
                    || mob instanceof Creeper || mob instanceof EnderMan
                    || mob instanceof AbstractPiglin || mob instanceof Guardian) {
                return false;
            }
            
            if (GoldExperienceCreateLifeform.getVolume(entity) >= 7.5 || mob.getMaxHealth() > 60) {
                return false;
            }
            
            ResourceLocation typeId = entity.getType().getRegistryName();
            if (DISABLE_SUMMON_MANUALLY_NAMESPACES.contains(typeId.getNamespace()) || DISABLE_SUMMON_MANUALLY.contains(typeId)) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    private static final Set<String> DISABLE_SUMMON_MANUALLY_NAMESPACES = Util.make(new HashSet<>(), set -> {
        set.add("rotp_zbc");
        set.add("rotp_harvest");
    });
    
    private static final ResLocSet DISABLE_SUMMON_MANUALLY = new ResLocSet()
            .add("twilightforest", 
                    "quest_ram",
                    "wraith",
                    "redcap",
                    "redcap_sapper",
                    "death_tome",
                    "minoshroom",
                    "minotaur",
                    "maze_slime",
                    "mist_wolf",
                    "tower_golem",
                    "blockchain_goblin",
                    "goblin_knight_upper",
                    "goblin_knight_lower",
                    "knight_phantom",
                    "yeti",
                    "snow_guardian",
                    "stable_ice_core",
                    "unstable_ice_core",
                    "snow_queen",
                    "ice_crystal",
                    "troll",
                    "adherent",
                    "roving_cube",
                    "plateau_boss"
                    )
            .add("alexsmobs",
                    "centipede_head",
                    "guster",
                    "enderiophage",
                    "mimicube"
                    )
            .add("rotp_zkq",
                    "sheer_heart");
    
    public static void unlockAllEntityTypes(Player player) {
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            EntitySubtype.values()
            .filter(type -> isValidLifeform(type, player.level))
            .forEach(entityType -> {
                cap.addMetEntityType(entityType);
            });
        });
    }
}
