package com.github.standobyte.jojo.entity.mob.rps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.ForgeEventFactory;

public class RockPaperScissorsKidEntity extends Villager implements IMobStandUser {
    private final IStandPower standPower = new StandPower(this);
    // TODO (BIIM) also keep pvp games (in the save file cap?)
    private Map<UUID, RockPaperScissorsGame> games = new HashMap<>();
    private Set<UUID> lostTo = new HashSet<>();
    @Nullable
    private RockPaperScissorsGame currentGame;
    @Nullable
    private UUID currentOpponent;

    public RockPaperScissorsKidEntity(Level world) {
        this(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), world);
    }

    public RockPaperScissorsKidEntity(EntityType<? extends Villager> type, Level world) {
        super(type, world);
    }

    public boolean isPlaying() {
        return this.currentGame != null;
    }

    // TODO (BIIM) make proper ai, remove this hack
    @Override
    public boolean isTrading() {
        return super.isTrading() || isPlaying();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isBaby() || player.isShiftKeyDown()) {
            if (hand == InteractionHand.MAIN_HAND) {
                startRockPaperScissorsGame(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    private void startRockPaperScissorsGame(Player player) {
        if (!level.isClientSide()) {
            RockPaperScissorsGame game = games.computeIfAbsent(player.getUUID(), 
                    opponentId -> new RockPaperScissorsGame(player, this));
            currentGame = game;
            currentOpponent = player.getUUID();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
            game.gameStarted((ServerLevel) level);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        // TODO (BIIM) random pick
        if (!level.isClientSide() && tickCount % 10 == 0) {
            makeRandomPick();
        }
        if (currentGame != null && (/*currentGame.playerLeft() || */currentGame.isGameOver())) {
            if (currentGame.isGameOver()) {
                games.remove(currentOpponent);
            }
            currentGame = null;
            currentOpponent = null;
        }
    }
    
    public void makeRandomPick() {
        if (currentGame != null) {
            Pick pick = currentGame.getRound() == 1 ? Pick.SCISSORS : Pick.values()[random.nextInt(Pick.values().length)];
            currentGame.makeAPick(this, pick, false);
        }
    }
    
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, 
            @Nullable SpawnGroupData additionalData, @Nullable CompoundTag nbt) {
        standPower.givePower(ModStandsInit.BOY_II_MAN.get());
        AgeableMob.AgeableData ageableData = new AgeableMob.AgeableData(1);
        ageableData.increaseGroupSizeByOne();
        additionalData = ageableData;
        
        return super.finalizeSpawn(world, difficulty, reason, additionalData, nbt);
    }

    @Override
    public IStandPower getStandPower() {
        return standPower;
    }
    
    @Override
    protected Component getTypeName() {
        return getType().getDescription();
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("StandPower", standPower.writeNBT());
        
        ListTag lostToNBT = new ListTag();
        lostTo.forEach(winner -> lostToNBT.add(NbtUtils.createUUID(winner)));
        nbt.put("LostTo", lostToNBT);

        CompoundTag unfinishedGamesNBT = new CompoundTag();
        games.forEach((playerUUID, game) -> unfinishedGamesNBT.put(playerUUID.toString(), game.writeNBT()));
        nbt.put("UnfinishedGames", unfinishedGamesNBT);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("StandPower", MCUtil.getNbtId(CompoundTag.class))) {
            standPower.readNBT(nbt.getCompound("StandPower"));
        }

        if (nbt.contains("LostTo", MCUtil.getNbtId(ListTag.class))) {
            nbt.getList("LostTo", MCUtil.getNbtId(IntArrayTag.class)).forEach(uuidNBT -> {
                if (uuidNBT != null && uuidNBT.getType() == IntArrayTag.TYPE && ((IntArrayTag) uuidNBT).getAsIntArray().length == 4) {
                    lostTo.add(NbtUtils.loadUUID(uuidNBT));
                }
            });
        }

        if (nbt.contains("UnfinishedGames", MCUtil.getNbtId(CompoundTag.class))) {
            CompoundTag unfinishedGamesNBT = nbt.getCompound("UnfinishedGames");
            unfinishedGamesNBT.getAllKeys().forEach(key -> {
                try {
                    UUID id = UUID.fromString(key);
                    if (unfinishedGamesNBT.contains(key, MCUtil.getNbtId(CompoundTag.class))) {
                        RockPaperScissorsGame game = RockPaperScissorsGame.fromNBT(unfinishedGamesNBT.getCompound(key));
                        if (game != null) {
                            games.put(id, game);
                        }
                    }
                }
                catch (IllegalArgumentException e) {}
            });
        }
    }
    
    
    
    public static boolean canTurnFromArrow(Entity entity) {
        if (entity.getType() == EntityType.VILLAGER) {
            Mob villager = (Mob) entity;
            return villager.isBaby() && villager.getRandom().nextDouble() < 0.5;
        }
        return false;
    }
    
    public static void turnFromArrow(Entity entity) {
        Level world = entity.level;
        if (!world.isClientSide()) {
            Mob villagerKid = (Mob) entity;
            if (ForgeEventFactory.canLivingConvert(villagerKid, ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), (timer) -> {})) {
                RockPaperScissorsKidEntity RPSkid = villagerKid.convertTo(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), true);
                RPSkid.finalizeSpawn(
                        (ServerLevel) world, 
                        world.getCurrentDifficultyAt(RPSkid.blockPosition()), 
                        MobSpawnType.CONVERSION, 
                        null, 
                        null);
                RPSkid.setVillagerData(RPSkid.getVillagerData().setType(VillagerType.byBiome(world.getBiomeName(RPSkid.blockPosition()))));
                ForgeEventFactory.onLivingConvert(villagerKid, RPSkid);
            }
        }
    }
}
