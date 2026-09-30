package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {

    public ServerPlayerEntityMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }
    
    @Inject(method = "doTick", at = @At("HEAD"), cancellable = true)
    public void jojoTsCancelPlayerTick(CallbackInfo ci) {
        if (!this.canUpdate()) {
            ci.cancel();
        }
    }
    
    
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"), cancellable = true)
    public void jojoCancelTeleport(ServerLevel pNewLevel, double pX, double pY, double pZ, float pYaw, float pPitch, CallbackInfo ci) {
        if (this instanceof IPlayerPossess) {
            IPlayerPossess player = (IPlayerPossess) this;
            Entity possessedEntity = player.jojoGetPossessedEntity();
            if (possessedEntity != null) {
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
    public void jojoCancelEntitySpectate(Entity entityToSpectate, CallbackInfo ci) {
        if (this instanceof IPlayerPossess) {
            IPlayerPossess player = (IPlayerPossess) this;
            Entity possessedEntity = player.jojoGetPossessedEntity();
            // TODO disable this, only allow using specific actions to un-possess an entity
            if (possessedEntity != null && possessedEntity != entityToSpectate) {
                if (player.jojoIsPossessingAsAlive() && entityToSpectate == this) {
                    player.jojoPossessEntity(null, true, null);
                }
                else {
                    ci.cancel();
                }
            }
        }
    }

}
