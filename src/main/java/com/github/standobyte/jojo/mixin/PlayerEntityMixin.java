package com.github.standobyte.jojo.mixin;

import com.github.standobyte.jojo.util.mc.damage.ModDamageTypes;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.capability.entity.player.PlayerMixinExtension;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPossessEntityPacket;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.IPlayerLeap;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import com.github.standobyte.jojo.init.power.RegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraft.nbt.Tag;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerMixinExtension, IPlayerLeap, IPlayerPossess {
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public void jojoMixinTick(CallbackInfo ci) {
        leapFlagTick();
        jojoTickEntityPossession();
    }
    
    @Override
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<MobType> ci) {
        if (JojoModUtil.playerUndeadAttribute((LivingEntity) (Object) this)) {
            ci.setReturnValue(MobType.UNDEAD);
        }
    }
    
    @Override
    public boolean _isEntityOnGround() {
        return onGround();
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void jojoPlayerWallClimb(Vec3 pTravelVector, CallbackInfo ci) {
        Player thisPlayer = (Player) (Object) this;
        if (HamonWallClimbing2.travelWallClimb(thisPlayer, pTravelVector)) {
            ci.cancel();
        }
    }
    

    private boolean isDoingLeap;
    @Override
    public void setIsDoingLeap(boolean isDoingLeap) {
        this.isDoingLeap = isDoingLeap;
    }
    
    @Override
    public boolean isDoingLeap() {
        return isDoingLeap;
    }
    
//    private boolean isDoingDash = false;
    
    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    public void jojoBackOffFromEdgeFlag(CallbackInfoReturnable<Boolean> ci) {
        if (isDoingLeap) {
            ci.setReturnValue(false);
        }
//        else if (isDoingDash) {
//            ci.setReturnValue(true);
//        }
    }
    
    
    
    private final EntityOwnerResolver jojoPossessedEntity = new EntityOwnerResolver();
    private Optional<GameType> jojoPossessPrevGameMode = Optional.empty();
    private boolean jojoPossessingAsAlive;
    private RegistryEntry<?> jojoPossessionContext;
    private boolean turnedIntoAngeloRock;
    
    /* TODO specific interactions when possessing someone with asAlive flag:
     *   render hp/hunger/etc.
     *   tick the player
     *     tick status effects
     *   allow the power HUD to be opened (remove JojoModUtil.tmpSpectatorCantUsePowers(LivingEntity))
     *   ...?
     */
    @Override
    public void jojoPossessEntity(@Nullable Entity entity, boolean asAlive, RegistryEntry<?> context) {
        if (entity == this) return;
        jojoPossessedEntity.setOwner(entity);
        if (!level.isClientSide()) {
            if (turnedIntoAngeloRock) {
                entity = null;
            }
            
            ServerPlayer player = ((ServerPlayer) (Entity) this);
            if (entity != null) {
                jojoPossessPrevGameMode = Optional.of(player.gameMode.getGameModeForPlayer());
                player.setGameMode(GameType.SPECTATOR);
                player.setCamera(entity);
            }
            else {
                jojoPossessPrevGameMode.ifPresent(player::setGameMode);
                jojoPossessPrevGameMode = Optional.empty();
                player.setCamera(player);
            }
            PacketManager.sendToClientsTrackingAndSelf(new TrPossessEntityPacket(
                    this.getId(), jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                    jojoPossessPrevGameMode, context), this);
            
            if (turnedIntoAngeloRock && player.isAlive()) {
                player.invulnerableTime = 0;
                player.hurt(ModDamageTypes.source(player, "rockBroken"), Float.MAX_VALUE);
                // FIXME (!!) https://bugs.mojang.com/browse/MC/issues/MC-161755 - what the fuck is going on here??
                if (player.isDeadOrDying()) {
                    player.discard();
                }
            }
        }
        this.jojoPossessingAsAlive = asAlive;
        this.jojoPossessionContext = context;
        turnedIntoAngeloRock = entity != null && entity.getType() == ModEntityTypes.ANGELO_ROCK.get();
    }
    
    private void jojoTickEntityPossession() {
        if (!level.isClientSide() && jojoPossessedEntity.hasEntityId()) {
            Entity possessed = jojoGetPossessedEntity();
            if (possessed == null || !possessed.isAlive() || !this.isAlive()) {
                jojoPossessEntity(null, jojoPossessingAsAlive, jojoPossessionContext);
            }
        }
    }
    
    @Override
    @Nullable public Entity jojoGetPossessedEntity() {
        return jojoPossessedEntity.getEntity(level);
    }
    
    @Override
    public boolean jojoIsPossessingAsAlive() {
        return jojoPossessingAsAlive;
    }
    
    @Override
    public Optional<GameType> jojoGetPrePossessGameMode() {
        return jojoPossessPrevGameMode;
    }
    
    @Override
    public void jojoSetPrePossessGameMode(@Nonnull Optional<GameType> gameMode) {
        this.jojoPossessPrevGameMode = gameMode;
        if (!level.isClientSide()) {
            PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                    jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                    jojoPossessPrevGameMode, jojoPossessionContext), ((ServerPlayer) (Entity) this));
        }
    }
    
    @Override
    public RegistryEntry<?> jojoGetPossessionContext() {
        return jojoPossessionContext;
    }
    
    @Override
    public void jojoOnPossessingDead() {
        if (!level.isClientSide() && getType() == EntityType.PLAYER) {
            Entity possessedEntity = jojoGetPossessedEntity();
            if (possessedEntity != null) {
                jojoPossessEntity(null, false, jojoPossessionContext);
                if (possessedEntity.getType() == ModEntityTypes.ANGELO_ROCK.get()) {
                    remove(((Entity) this) instanceof ServerPlayer);
                }
            }
        }
    }
    
    
    @Override
    public void toNBT(CompoundTag forgeCapNbt) {
        jojoPossessedEntity.saveNbt(forgeCapNbt, "Possessed");
        jojoPossessPrevGameMode.ifPresent(gameMode -> forgeCapNbt.putString("PossessPrevMode", gameMode.getName()));
        forgeCapNbt.putBoolean("PossessAsAlive", jojoPossessingAsAlive);
        if (jojoPossessionContext != null) {
            CompoundTag ctxNbt = new CompoundTag();
            IForgeRegistry<?> retrievedRegistry = MCUtil.getRegistry(jojoPossessionContext);
            if (retrievedRegistry != null) {
                ctxNbt.putString("Registry", retrievedRegistry.getRegistryName().toString());
                ctxNbt.putString("Obj", jojoPossessionContext.getRegistryName().toString());
                forgeCapNbt.put("Ctx", ctxNbt);
            }
        }
    }
    
    @Override
    public void fromNBT(CompoundTag forgeCapNbt) {
        jojoPossessedEntity.loadNbt(forgeCapNbt, "Possessed");
        jojoPossessPrevGameMode = Optional.ofNullable(GameType.byName(forgeCapNbt.getString("PossessPrevMode"), null));
        jojoPossessingAsAlive = forgeCapNbt.getBoolean("PossessAsAlive");
        jojoPossessionContext = MCUtil.nbtGetCompoundOptional(forgeCapNbt, "Ctx").map(ctxNbt -> {
            if (ctxNbt.contains("Registry", Tag.TAG_STRING) && ctxNbt.contains("Obj", Tag.TAG_STRING)) {
                ResourceLocation registryId = new ResourceLocation(ctxNbt.getString("Registry"));
                ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(registryId);
                if (registry != null) {
                    ResourceLocation objId = new ResourceLocation(ctxNbt.getString("Obj"));
                    if (registry.containsKey(objId)) {
                        return registry.getValue(objId);
                    }
                }
            }
            
            return null;
        }).orElse(null);
    }

    @Override
    public void syncToClient(ServerPlayer thisAsPlayer) {
        PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                jojoPossessPrevGameMode, jojoPossessionContext), thisAsPlayer);
        Entity cameraEntity = jojoPossessedEntity.getEntity(level);
        if (cameraEntity != null) {
            thisAsPlayer.setCamera(cameraEntity);
        }
        turnedIntoAngeloRock = cameraEntity != null && cameraEntity.getType() == ModEntityTypes.ANGELO_ROCK.get();
    }

    @Override
    public void syncToTracking(ServerPlayer tracking) {
        PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                Optional.empty(), null), tracking);
    }
}
