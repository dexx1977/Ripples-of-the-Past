package com.github.standobyte.jojo.block;

import static net.minecraft.block.BedBlock.OCCUPIED;
import static net.minecraft.block.BedBlock.PART;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.GameplayEventHandler;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.mojang.datafixers.util.Either;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

public class WoodenCoffinBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty CLOSED = BooleanProperty.create("coffin_lid_closed");
    private final DyeColor color;

    public WoodenCoffinBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, false).setValue(CLOSED, false));
    }

    @Override
    public boolean isBed(BlockState state, BlockGetter world, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, 
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide) {
            if (!BedBlock.canSetSpawn(world)) {
                Random random = world.random;
                int particlesSetting = ClientUtil.particlesSetting();
                if (particlesSetting < 2) {
                    int particles = particlesSetting == 1 ? 256 : 2560;
                    for (int i = 0; i < particles; i++) {
                        ClientUtil.getClientWorld().addParticle(ModParticles.BLOOD.get(), 
                                blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 
                                (random.nextDouble() - 0.5) * 1.5, 
                                (random.nextDouble() - 0.5) * 1.5, 
                                (random.nextDouble() - 0.5) * 1.5);
                    }
                }
            }
            return InteractionResult.CONSUME;
        } else {
            if (blockState.getValue(PART) != BedPart.HEAD) {
                blockPos = blockPos.relative(blockState.getValue(FACING));
                blockState = world.getBlockState(blockPos);
                if (!blockState.is(this)) {
                    return InteractionResult.CONSUME;
                }
            }
            if (!BedBlock.canSetSpawn(world)) {
                world.removeBlock(blockPos, false);
                BlockPos neighborPos = blockPos.relative(blockState.getValue(FACING).getOpposite());
                if (world.getBlockState(neighborPos).is(this)) {
                    world.removeBlock(neighborPos, false);
                }

                world.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(6), 
                        EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS))
                .forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
                    entity.clearFire();
                });
                world.explode(null, DamageSource.badRespawnPointExplosion(), null, 
                        blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 5.0F, false, Explosion.BlockInteraction.DESTROY);
                GameplayEventHandler.splashBlood(world, Vec3.atCenterOf(blockPos), 16, 10, Optional.empty());
                
                return InteractionResult.SUCCESS;
            } else {
                sleepInsideCoffin(player, world, blockPos, blockState, false);
                return InteractionResult.SUCCESS;
            }
        }
    }
    
    private static void sleepInsideCoffin(Player player, Level world, 
            BlockPos blockPos, BlockState blockState, boolean vampireRespawn) {
        boolean occupied = blockState.getValue(OCCUPIED);
        if (player.isShiftKeyDown() || occupied) {
            world.setBlock(blockPos, blockState.setValue(CLOSED, !blockState.getValue(CLOSED)), 3);
            if (!player.isShiftKeyDown() && occupied) {
                player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }
        } else {
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(
                    playerData -> playerData.onSleepingInCoffin(vampireRespawn));
            BlockPos coffinPos = blockPos;
            Either<Player.SleepResult, Unit> sleepResult = player.startSleepInBed(blockPos);
            sleepResult.ifLeft(failed -> {
                if (failed != null) {
                    if (!world.isClientSide() && failed == Player.SleepResult.NOT_SAFE) {
                        forseSleep((ServerPlayer) player, coffinPos);
                    }
                    else {
                        player.displayClientMessage(failed.getMessage(), true);
                    }
                }
            });
            if (player.isSleeping()) {
                Vec3 sleepingPos = new Vec3(
                        blockPos.getX() + 0.5, 
                        blockPos.getY() + 0.1875, 
                        blockPos.getZ() + 0.5);
                Direction dir = blockState.getBedDirection(world, coffinPos);
                sleepingPos = sleepingPos.add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.085));
                player.teleportTo(sleepingPos.x, sleepingPos.y, sleepingPos.z);
            }
        }
    }
    
    private static void forseSleep(ServerPlayer player, BlockPos blockPos) {
        player.startSleeping(blockPos);
        CommonReflection.setSleepCounter(player, 0);
        player.awardStat(Stats.SLEEP_IN_BED);
        CriteriaTriggers.SLEPT_IN_BED.trigger(player);
        ((ServerLevel) player.level).updateSleepingPlayerList();
    }
    
    @Override
    public void setBedOccupied(BlockState state, Level world, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        super.setBedOccupied(state, world, pos, sleeper, occupied);
        world.setBlock(pos, state.setValue(OCCUPIED, occupied).setValue(CLOSED, occupied), 3);
    }
    

    @EventBusSubscriber(modid = JojoMod.MOD_ID)
    public static class EventHandler {
    
        @SubscribeEvent
        public static void setRespawnLocation(PlayerSetSpawnEvent event) {
            if (isBlockCoffin(event.getEntity().level, Optional.ofNullable(event.getNewSpawn()))
                    && !isEntityVampire(event.getPlayer())) {
                event.setCanceled(true);
            }
        }
        
        @SubscribeEvent
        public static void canSleepAtTime(SleepingTimeCheckEvent event) {
            if (isBlockCoffin(event.getEntity().level, event.getSleepingLocation())) {
                event.setResult(Result.ALLOW);
            }
        }
        
        @SubscribeEvent
        public static void setCoffinTime(SleepFinishedTimeEvent event) {
            if (event.getWorld() instanceof ServerLevel) {
                ServerLevel world = (ServerLevel) event.getWorld();
                int playersCount = world.players().size();
                if (world.players().stream()
                        .filter(player -> player.isSleeping() && isBlockCoffin(player.level, Optional.of(player.blockPosition())))
                        .count() >= (playersCount + 1) / 2) {
                    long time = world.getDayTime();
                    long timeAdded = (24000L - time % 24000L + 12600L) % 24000L;
                    event.setTimeAddition(time + timeAdded);
                }
            }
        }
        
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void skippedToNight(SleepFinishedTimeEvent event) {
            if (event.getWorld() instanceof ServerLevel) {
                ServerLevel world = (ServerLevel) event.getWorld();
                world.players().stream()
                .filter(player -> player.isSleeping())
                .forEach(player -> {
                    boolean isCoffin = isBlockCoffin(player.level, Optional.of(player.blockPosition()));
                    
                    if (isCoffin) {
                        if (player.hasEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get())) {
                            player.removeEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get());
                            player.removeEffect(MobEffects.WEAKNESS);
                        }
                        long oldTime = event.getWorld().getLevelData().getDayTime();
                        int oldDayTime = (int) (oldTime % 24000L);
                        int newDayTime = (int) (event.getNewTime() % 24000L);
                        if (newDayTime >= 12600 && newDayTime < 23500 && 
                                (oldDayTime < 12600 || oldDayTime >= 23500)) {
                            ModCriteriaTriggers.SLEPT_IN_COFFIN.get().trigger(player);
                        }
                    }
                });
            }
        }
        
        @SubscribeEvent
        public static void onServerPlayerRespawn(PlayerRespawnEvent event) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            respawnInsideCoffin(player, player.getRespawnPosition());
        }
        
        public static void respawnInsideCoffin(Player player, BlockPos respawnPos) {
            if (isEntityVampire(player) && respawnPos != null) {
                BlockState blockState = player.level.getBlockState(respawnPos);
                if (blockState.getBlock() instanceof WoodenCoffinBlock) {
                    if (!player.level.isClientSide()) {
                        sleepInsideCoffin(player, player.level, respawnPos, blockState, true);
                    }
                }
            }
        }
    }
    
    public static boolean isSleepingInCoffin(LivingEntity entity) {
        return isBlockCoffin(entity.level, entity.getSleepingPos());
    }
    
    public static boolean isBlockCoffin(Level world, Optional<BlockPos> blockPos) {
        return blockPos.map(pos -> world.getBlockState(pos).getBlock() instanceof WoodenCoffinBlock).orElse(false);
    }
    
    private static boolean isEntityVampire(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getType() == ModPowers.VAMPIRISM.get()).orElse(false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (direction == getNeighbourDirection(state.getValue(PART), state.getValue(FACING))) {
            return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART) ? 
                    state.setValue(OCCUPIED, neighborState.getValue(OCCUPIED)).setValue(CLOSED, neighborState.getValue(CLOSED))
                    : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    private static Direction getNeighbourDirection(BedPart p_208070_0_, Direction p_208070_1_) {
        return p_208070_0_ == BedPart.FOOT ? p_208070_1_ : p_208070_1_.getOpposite();
    }

    @Override
    public void playerWillDestroy(Level p_176208_1_, BlockPos p_176208_2_, BlockState p_176208_3_, Player p_176208_4_) {
        if (!p_176208_1_.isClientSide && p_176208_4_.isCreative()) {
            BedPart bedpart = p_176208_3_.getValue(PART);
            if (bedpart == BedPart.FOOT) {
                BlockPos blockpos = p_176208_2_.relative(getNeighbourDirection(bedpart, p_176208_3_.getValue(FACING)));
                BlockState blockstate = p_176208_1_.getBlockState(blockpos);
                if (blockstate.getBlock() == this && blockstate.getValue(PART) == BedPart.HEAD) {
                    p_176208_1_.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    p_176208_1_.levelEvent(p_176208_4_, 2001, blockpos, Block.getId(blockstate));
                }
            }
        }

        super.playerWillDestroy(p_176208_1_, p_176208_2_, p_176208_3_, p_176208_4_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
        Direction direction = p_196258_1_.getHorizontalDirection();
        BlockPos blockpos = p_196258_1_.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction);
        return p_196258_1_.getLevel().getBlockState(blockpos1).canBeReplaced(p_196258_1_) ? this.defaultBlockState().setValue(FACING, direction) : null;
    }

    private static final VoxelShape WALL_1 = Block.box(0, 0, 0, 2, 10, 16);
    private static final VoxelShape WALL_2 = Block.box(14, 0, 0, 16, 10, 16);
    private static final VoxelShape WALL_3 = Block.box(0, 0, 0, 16, 10, 2);
    private static final VoxelShape WALL_4 = Block.box(0, 0, 14, 16, 10, 16);
    private static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 2, 16);
    
    protected static final VoxelShape SHAPE_CLOSED = Block.box(0, 0, 0, 16, 13, 16);
    protected static final VoxelShape SHAPE_OPEN_W = Shapes.or(BOTTOM, WALL_2, WALL_3, WALL_4);
    protected static final VoxelShape SHAPE_OPEN_E = Shapes.or(BOTTOM, WALL_1, WALL_3, WALL_4);
    protected static final VoxelShape SHAPE_OPEN_N = Shapes.or(BOTTOM, WALL_1, WALL_2, WALL_4);
    protected static final VoxelShape SHAPE_OPEN_S = Shapes.or(BOTTOM, WALL_1, WALL_2, WALL_3);
    @Deprecated
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos pos, CollisionContext p_220053_4_) {
        if (blockState.getValue(CLOSED)) {
            return SHAPE_CLOSED;
        }
        Direction direction = blockState.getValue(FACING);
        BedPart part = blockState.getValue(PART);
        if (part == BedPart.HEAD) direction = direction.getOpposite();
        switch (direction) {
        case NORTH:
            return SHAPE_OPEN_N;
        case EAST:
            return SHAPE_OPEN_E;
        case SOUTH:
            return SHAPE_OPEN_S;
        case WEST:
            return SHAPE_OPEN_W;
        default:
            break;
        }
        return super.getShape(blockState, world, pos, p_220053_4_);
    }

    public static Direction getConnectedDirection(BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        return blockState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
        p_206840_1_.add(FACING, PART, OCCUPIED, CLOSED);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity entity, ItemStack item) {
        super.setPlacedBy(world, blockPos, blockState, entity, item);
        if (!world.isClientSide) {
            BlockPos blockpos = blockPos.relative(blockState.getValue(FACING));
            world.setBlock(blockpos, blockState.setValue(PART, BedPart.HEAD), 3);
            world.blockUpdated(blockPos, Blocks.AIR);
            blockState.updateNeighbourShapes(world, blockPos, 3);
        }

    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public long getSeed(BlockState p_209900_1_, BlockPos p_209900_2_) {
        BlockPos blockpos = p_209900_2_.relative(p_209900_1_.getValue(FACING), p_209900_1_.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(blockpos.getX(), p_209900_2_.getY(), blockpos.getZ());
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, BlockGetter p_196266_2_, BlockPos p_196266_3_, PathComputationType p_196266_4_) {
        return false;
    }
    
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 5;
    }
    
    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 5;
    }
    

}
