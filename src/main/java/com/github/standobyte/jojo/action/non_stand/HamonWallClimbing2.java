package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopWallClimbPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.OptionalFloat;
import com.github.standobyte.jojo.util.mc.CollisionUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class HamonWallClimbing2 extends HamonAction {

    public HamonWallClimbing2(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.areHandsFree(user, InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        return user.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> {
            if (target.getType() == TargetType.BLOCK && target.getFace() != null && target.getFace().getAxis() != Direction.Axis.Y) {
                Direction blockFace = target.getFace();
//                BlockPos standingOn = user.blockPosition().below().offset(blockFace.getOpposite().getNormal());
//                if (user.isOnGround() && standingOn.equals(target.getBlockPos())) {
//                    return ActionConditionResult.POSITIVE;
//                }
                Vec3 vecToBlock = Vec3.atLowerCornerOf(blockFace.getOpposite().getNormal()).scale(MAX_WALL_DISTANCE);
                Vec3 collide = collide(user, user.getBoundingBox(), vecToBlock, true);
                if (!collide.equals(vecToBlock)) {
                    return ActionConditionResult.POSITIVE;
                }
            }
            
            return ActionConditionResult.NEGATIVE;
        })
        .orElse(ActionConditionResult.NEGATIVE);
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return LivingWallClimbing.getHandler(power.getUser()).map(cap -> cap.isWallClimbing()).orElse(false);
    }
    
    @Override
    protected void perform(Level world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            LivingWallClimbing.getHandler(user).ifPresent(cap -> {
                if (target.getType() == TargetType.BLOCK && target.getFace() != null && target.getFace().getAxis() != Direction.Axis.Y) {
                    Direction face = target.getFace();
                    float yRot = 180 - face.toYRot();
                    if (!cap.isWallClimbing() || cap.getWallClimbYRot().orElseGet(() -> yRot) != yRot) {
                        Vec3 vecToBlock = Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(MAX_WALL_DISTANCE);
                        Vec3 collide = collide(user, user.getBoundingBox(), vecToBlock, true);
                        double distanceFromWall = user.getBbWidth() * 0.15;
                        Vec3 moveTo = user.position().add(collide).add(Vec3.atLowerCornerOf(face.getNormal()).scale(distanceFromWall));
                        user.teleportTo(moveTo.x, moveTo.y, moveTo.z);
                        
                        cap.setWallClimbing(true, true, -1, OptionalFloat.of(yRot));
                        if (user instanceof Player) {
                            ((Player) user).displayClientMessage(Component.translatable(
                                    "jojo.message.wall_climb.hint_jump", Component.keybind("key.jump")), true);
                        }
                        return;
                    }
                }
            });
        }
    }
    
    private static final double SIDE_SPEED_MULT = 0.5;
    private static final double DOWN_SPEED_MULT = 0.5;
    private static final double SIDE_VEC_LEN_MULT = 1 / Math.sqrt(1 + SIDE_SPEED_MULT * SIDE_SPEED_MULT);
    private static final double DOWN_SIDE_VEC_LEN_MULT = Math.max(DOWN_SPEED_MULT, SIDE_SPEED_MULT) / Math.sqrt(DOWN_SPEED_MULT * DOWN_SPEED_MULT + SIDE_SPEED_MULT * SIDE_SPEED_MULT);
    private static final double MAX_WALL_DISTANCE = 0.5;
    public static boolean travelWallClimb(Player player, Vec3 inputVec) {
        Optional<LivingWallClimbing> playerData = LivingWallClimbing.getHandler(player);
        boolean isWallClimbing = playerData.map(cap -> cap.isWallClimbing()).orElse(false);
        if (isWallClimbing) {
            player.fallDistance = 0;
            
            LivingWallClimbing wallClimbData = playerData.get();
            float climbYRot = wallClimbData.getWallClimbYRot().orElseGet(() -> player.yBodyRot) * MathUtil.DEG_TO_RAD;
            Vec3 gripVec = new Vec3(0, 0, MAX_WALL_DISTANCE).yRot(climbYRot);
            
            if (!MCUtil.itemHandFree(player.getItemInHand(InteractionHand.MAIN_HAND)) || !MCUtil.itemHandFree(player.getItemInHand(InteractionHand.OFF_HAND))
                    || player.isSpectator()) {
                stopWallClimbing(player, wallClimbData);
                return false;
            }

            Vec3 collide = collide(player, player.getBoundingBox(), gripVec, true);
            if (collide.subtract(gripVec).lengthSqr() < 1E-7) {
                stopWallClimbing(player, wallClimbData);
                return false;
            }
            
            if (player.isLocalPlayer()) {
//                double xPrev = player.getX();
//                double yPrev = player.getY();
//                double zPrev = player.getZ();
                double climbSpeed = wallClimbData.getWallClimbSpeed() * player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                boolean canPullUp = false;
                
                Vec3 movement = new Vec3(inputVec.x * SIDE_SPEED_MULT, inputVec.z > 0 ? inputVec.z : inputVec.z * DOWN_SPEED_MULT, 0);
                if (movement.lengthSqr() > 1) {
                    movement = movement.normalize();
                }
                if (inputVec.x != 0 && inputVec.z != 0) {
                    if (inputVec.z > 0) {
                        climbSpeed *= SIDE_VEC_LEN_MULT;
                    }
                    else {
                        climbSpeed *= DOWN_SIDE_VEC_LEN_MULT;
                    }
                }
                movement = movement.scale(climbSpeed);
                climbSpeed = movement.length();
                if (climbYRot != 0) {
                    movement = movement.yRot(climbYRot);
                }

                Vec3 horizontalMovementOnly = new Vec3(movement.x, 0, movement.z);
                Vec3 collideAfterMove;
                AABB gripBox = player.getBoundingBox()
                        .contract(0, -player.getBbHeight() * 0.5, 0);
                if (movement.y < 0) {
                    // stop climbing if standing on a solid block
                    if (player.onGround()) {
                        stopWallClimbing(player, wallClimbData);
                        return false;
                    }
                    else {
                        // make sure the player doesn't fall down
                        collideAfterMove = collide(player, 
                                gripBox.move(0, -gripBox.getYsize() + movement.y, 0), 
                                gripVec, true);
                        if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                            movement = horizontalMovementOnly;
                        }
                    }
                }
                
                // check if the player is at the top of a wall
                collideAfterMove = collide(player, gripBox.move(0, gripBox.getYsize(), 0), gripVec, true);
                if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                    if (movement.y > 0) {
                        movement = horizontalMovementOnly;
                    }
                    canPullUp = true;
                }
                
                // make sure the player doesn't fall while moving to the left/right if a horizontal end of a wall is reached
                collideAfterMove = collide(player, 
                        gripBox.move(horizontalMovementOnly.normalize()
                                .scale(horizontalMovementOnly.length() + player.getBbWidth() + 0.1)),
                        gripVec, true);
                if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                    movement = new Vec3(0, movement.y, 0);
                }
                
//                if (player.isLocalPlayer()) {
                    LocalPlayer clientPlayer = (LocalPlayer) player; // monkaS
                    boolean isJumping = clientPlayer.input.jumping;
                    if (isJumping) {
                        stopWallClimbing(player, wallClimbData);
                        // the only check that also has to consider barriers, since the player is not supposed to be able to pull up
                        canPullUp &= collide(player, gripBox.move(0, gripBox.getYsize(), 0), gripVec, false).subtract(gripVec).lengthSqr() < 1E-7;
                        if (canPullUp) {
                            // TODO pulling up animation?
                            player.move(MoverType.SELF, new Vec3(0, player.getBbHeight(), 0));
//                            Vector3d pullUpMovement = new Vector3d(0, player.getBbHeight() + 0.1, 0);
//                            player.setDeltaMovement(pullUpMovement);
                            player.setDeltaMovement(new Vec3(0, 0, 0.1).yRot(climbYRot));
                        }
                        return false;
                    }
                    
                    boolean isMoving = movement.lengthSqr() > 1E-7;
                    ClientUtil.setPlayerHandsBusy(player, isMoving);
                    double up = movement.y;
                    double left = movement.yRot(-climbYRot).x;
                    InputHandler.getInstance().wallClimbClientTick(isMoving, wallClimbData);
                    
                    float animSpeed = (float) climbSpeed / MIN_MOVEMENT_SPEED;
                    ModPlayerAnimations.wallClimbing.tickAnimProperties(player, isMoving, 
                            up, left, animSpeed);
//                }
                
                player.setDeltaMovement(movement);
                player.move(MoverType.SELF, player.getDeltaMovement());
                movement = player.getDeltaMovement();
                
                player.calculateEntityAnimation(player, false);

//                player.checkMovementStatistics(player.getX() - xPrev, player.getY() - yPrev, player.getZ() - zPrev); // doesn't do anything anyway
                return true;
            }
        }
        return false;
    }
    private static final float MIN_MOVEMENT_SPEED = 0.06f;
    
    public static final CollisionContext NO_CLIMBING_ON_BARRIERS = new CollisionContext() {
        @Override public boolean isDescending() { return false; }
        @Override public boolean isAbove(VoxelShape pShape, BlockPos pPos, boolean pCanAscend) { return false; }
        @Override public boolean isHoldingItem(Item pItem) { return false; }
        @Override public boolean canStandOnFluid(FluidState pState, FluidState pFlowing) { return false; }
    };
    
    private static Vec3 collide(Entity entity, AABB collisionBox, Vec3 offsetVec, boolean excludeBarriers) {
        return CollisionUtil.collide(entity, collisionBox, offsetVec, excludeBarriers ? NO_CLIMBING_ON_BARRIERS : null);
    }
    
    /**
     * Called from {@link com.github.standobyte.jojo.mixin.BarrierBlockWallClimbMixin#changeCollisionShape}
     */
    public static boolean disableBlockCollisionShape(CollisionContext ctx) {
        return ctx == NO_CLIMBING_ON_BARRIERS;
    }
    
    private static void stopWallClimbing(Player player, LivingWallClimbing wallClimbing) {
        if (!player.level.isClientSide()) {
            wallClimbing.stopWallClimbing();
        }
        else if (player.isLocalPlayer()) {
            PacketManager.sendToServer(new ClStopWallClimbPacket());
        }
    }
    
    public double getHamonWallClimbSpeed(LivingEntity player) {
        Optional<INonStandPower> powerOptional = INonStandPower.getNonStandPowerOptional(player).resolve();
        Optional<HamonData> hamonOptional = powerOptional.flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
        return hamonOptional.map(hamon -> {
            INonStandPower power = powerOptional.get();
            double speed = (1.2 + hamon.getBreathingLevel() * 0.004 + hamon.getHamonControlLevel() * 0.00667)
                    * hamon.getActionEfficiency(power.getMaxEnergy() / 2, false, ModHamonSkills.WALL_CLIMBING.get());
            return speed;
        }).orElse(0.0);
    }
    
    public float getTickEnergyCost(INonStandPower power, boolean isMoving) {
        float cost = getHeldTickEnergyCost(power);
        if (!isMoving) {
            cost *= 0.25f;
        }
        return cost;
    }
    
    
    public static void tickWallClimbing(INonStandPower power, HamonData hamon, LivingEntity user) {
        LivingWallClimbing.getHandler(user).ifPresent(wallClimbData -> {
            if (wallClimbData.isHamon()) {
                if (hamon.isSkillLearned(ModHamonSkills.WALL_CLIMBING.get())) {
                    boolean isMoving = false;
                    if (user instanceof Player) {
                        isMoving = wallClimbData.wallClimbIsMoving;
                    }

                    if (power.getHeldAction() != ModHamonActions.HAMON_BREATH.get()) {
                        boolean consumedEnergy = false;

                        float energyCost = ModHamonActions.HAMON_WALL_CLIMBING.get().getTickEnergyCost(power, isMoving);
                        float points = Math.min(energyCost, power.getEnergy() * hamon.getActionEfficiency(energyCost, false, ModHamonSkills.WALL_CLIMBING.get()));
                        if (power.hasEnergy(energyCost)) {
                            power.consumeEnergy(energyCost);
                            consumedEnergy = true;
                            if (isMoving && !user.level.isClientSide()) {
                                hamon.hamonPointsFromAction(HamonStat.CONTROL, points);
                            }
                        }
                        
                        if (!consumedEnergy) {
                            if (!user.level.isClientSide()) {
                                wallClimbData.stopWallClimbing();
                            }
                        }
                    }
                    
                    if (user.level.isClientSide()) {
                        HamonSparksLoopSound.playSparkSound(user, new Vec3(user.getX(), user.getY(0.75), user.getZ()), 1.0F, true);
                    }
                }
                else if (!user.level.isClientSide()) {
                    wallClimbData.stopWallClimbing();
                }
            }
        });
    }
    
}
