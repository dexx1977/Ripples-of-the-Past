package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.entity.IPassengerMixinReposition;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public Level level;

//    @Shadow private int portalCooldown;
//    @Shadow protected int portalTime;
    
    private KnockbackCollisionImpact jojoKbCollision;
    private boolean repeatCollide;
    
    @Inject(method = "collide", at = @At("TAIL"), cancellable = true)
    public void jojoCollideBreakBlocks(Vec3 movementVec, CallbackInfoReturnable<Vec3> ci) {
        if (repeatCollide) {
            repeatCollide = false;
            return;
        }
        if (jojoKbCollision == null) {
            jojoKbCollision = KnockbackCollisionImpact.getHandler((Entity) (Object) this).orElse(null);
        }
        if (jojoKbCollision != null) {
            if (jojoKbCollision.collideBreakBlocks(movementVec, ci.getReturnValue(), level)) {
                repeatCollide = true;
                Vec3 repeatCollide = collide(movementVec);
                ci.setReturnValue(repeatCollide);
            }
        }
    }
    
    @Shadow
    protected abstract Vec3 collide(Vec3 pVec);
    
    
    
    @Redirect(method = "updateFluidHeightAndDoFluidPushing", at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/fluid/FluidState;getFlow("
                    + "Lnet/minecraft/world/IBlockReader;"
                    + "Lnet/minecraft/util/math/BlockPos;)"
                    + "Lnet/minecraft/util/math/vector/Vector3d;"))
    public Vec3 jojoTsCancelFluidPush(FluidState fluidState, BlockGetter world, BlockPos blockPos) {
        if (TimeStopHandler.isTimeStopped(level, blockPos)) {
            return Vec3.ZERO;
        }
        return fluidState.getFlow(world, blockPos);
    }
    
    
    
    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("TAIL"))
    public void jojoRepositionPassenger(Entity passenger, Entity.MoveFunction moveMethod, CallbackInfo ci) {
        Entity thisAsEntity = (Entity) (Object) this;
        if (passenger instanceof IPassengerMixinReposition && thisAsEntity.hasPassenger(passenger)) {
            Vec3 passengerPosition = ((IPassengerMixinReposition) passenger).repositionPassenger(thisAsEntity);
            if (passengerPosition != null) {
                moveMethod.accept(passenger, passengerPosition.x, passengerPosition.y, passengerPosition.z);
            }
        }
    }
}
