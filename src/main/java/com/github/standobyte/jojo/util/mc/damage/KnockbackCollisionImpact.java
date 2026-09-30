package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.util.mc.damage.ModDamageTypes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack.HeavyPunchBlockInstance.HeavyPunchExplosion;
import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.BlockShardEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.ResolveCounter;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.CollideBlocks;
import com.github.standobyte.jojo.util.mc.CollideBlocks.BlockCollisionResult;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.AxisCycle;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

public class KnockbackCollisionImpact implements INBTSerializable<CompoundTag> {
    private final Entity entity;
    private final LivingEntity asLiving;

    private LivingEntity attacker;
    private LivingEntity attackerStandUser;
    private boolean attackerIsStand;
    private Vec3 knockbackVec = null;
    private double knockbackImpactStrength;
    private double minCos;
    private boolean hadImpactWithBlock = false;
    private Vec3 prevTickPos;
    
    private float explosionRadius = 0;
    private DamageSource explosionDmgSource;
    private float explosionDamage;
    public List<BlockPos> blocksDestroyedByLastExplosion;
    
    private float syoPunchBaseDamage = 0;
    private int scarletOverdriveFireTicks = 0;
    private ParticleOptions hamonParticles;
    
    public KnockbackCollisionImpact(Entity entity) {
        this.entity = entity;
        this.asLiving = entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
    
    
    /**
     * @return true if the block collision needs to be recalculated
     */
    public boolean collideBreakBlocks(Vec3 movementVec, Vec3 collidedVec, Level world) {
        if (!isActive() || movementVec.lengthSqr() < 1E-07) {
            return false;
        }
        
        boolean canBreakBlocks = JojoModUtil.breakingBlocksEnabled(world);
        boolean collidedWithBlocks = !movementVec.equals(collidedVec);
        collideBoundingBox(entity, movementVec, collidedWithBlocks, canBreakBlocks);
        return canBreakBlocks && collidedWithBlocks;
    }
    
    public KnockbackCollisionImpact onPunchSetKnockbackImpact(Vec3 knockbackVec, LivingEntity attacker) {
        double kbMultiplier = 1 - (asLiving != null ? MCUtil.getValueIfPresent(asLiving, Attributes.KNOCKBACK_RESISTANCE, 0) : 0);
        if (kbMultiplier <= 0) return this;
        
        this.knockbackImpactStrength = knockbackVec.length();
        this.knockbackVec = knockbackVec.scale(1 / knockbackImpactStrength);
        this.knockbackImpactStrength *= kbMultiplier;
        this.minCos = 1;
        this.hadImpactWithBlock = false;
        this.attacker = attacker;
        this.attackerStandUser = attacker instanceof LivingEntity ? (StandUtil.getStandUser((LivingEntity) attacker)) : null;
        this.attackerIsStand = attacker instanceof StandEntity;
        this.blocksDestroyedByLastExplosion = null;
        return this;
    }
    
    public KnockbackCollisionImpact withImpactExplosion(float radius, DamageSource aoeDamageSource, float aoeDamage) {
        if (this.knockbackVec == null) return this;
        
        this.explosionRadius = radius;
        // the caller passes a source whose type is in the explosion tag
        this.explosionDmgSource = aoeDamageSource;
        this.explosionDamage = aoeDamage;
        return this;
    }
    
    public KnockbackCollisionImpact hamonDamage(float punchBaseDamage, int fireTicks, ParticleOptions sparkParticles) {
        if (this.knockbackVec == null) return this;
        
        this.syoPunchBaseDamage = punchBaseDamage;
        this.scarletOverdriveFireTicks = fireTicks;
        this.hamonParticles = sparkParticles;
        return this;
    }
    
    public void reset() {
        this.knockbackVec = null;
        this.knockbackImpactStrength = 0;
        this.explosionRadius = 0;
        this.explosionDmgSource = null;
        this.explosionDamage = 0;
        this.syoPunchBaseDamage = 0;
        this.scarletOverdriveFireTicks = 0;
        this.hamonParticles = null;
    }
    
    public void setKnockbackImpactStrength(double strength) {
        if (strength <= 0) {
            reset();
        }
        else {
            this.knockbackImpactStrength = strength;
        }
    }
    
    public void tick() {
        if (isActive()) {
            if (knockbackImpactStrength <= 0) {
                reset();
                return;
            }
            
            Vec3 deltaMovement = entity.getDeltaMovement();
            if (Math.abs(deltaMovement.x) < 1E-7 && Math.abs(deltaMovement.z) < 1E-7) {
                reset();
                return;
            }
            
            double deltaMovementLen = deltaMovement.length();
            Vec3 deltaMovementNormalized = deltaMovement.scale(1 / deltaMovementLen);
            double cos = deltaMovementNormalized.dot(knockbackVec);
            if (cos <= 0) {
                reset();
                return;
            }
            
            minCos = Math.min(minCos, cos);
            knockbackImpactStrength = Math.min(knockbackImpactStrength, deltaMovementLen);
            
            // spiders get stuck in cave corners not triggering the impact, so we try to manually trigger it here
            Vec3 entityPos = entity.position();
            if (prevTickPos != null && Math.abs(prevTickPos.x - entityPos.x) < 1E-7 && Math.abs(prevTickPos.z - entityPos.z) < 1E-7) {
                collideBreakBlocks(deltaMovement, deltaMovement, entity.level);
            }
            prevTickPos = entityPos;
        }
    }
    
    public double getKnockbackImpactStrength() {
        return knockbackImpactStrength * minCos;
    }
    
    public void setHadImpactWithBlock() {
        hadImpactWithBlock = true;
    }
    
    public boolean getHadImpactWithBlock() {
        return hadImpactWithBlock;
    }
    
    public boolean isActive() {
        return knockbackVec != null;
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (isActive()) {
            MCUtil.nbtPutVec3d(nbt, "Vec", knockbackVec);
            nbt.putDouble("Power", knockbackImpactStrength);
            nbt.putDouble("MinCos", minCos);
            nbt.putBoolean("HadBlockImpact", hadImpactWithBlock);
            nbt.putFloat("ExplosionRadius", explosionRadius);
            nbt.putFloat("ExplosionDamage", explosionDamage);
            nbt.putFloat("HamonPunchDmg", syoPunchBaseDamage);
            nbt.putInt("HamonFireTicks", scarletOverdriveFireTicks);
            if (hamonParticles instanceof ParticleType) {
                nbt.putString("HamonSparks", MCUtil.id(((ParticleType<?>) hamonParticles)).toString());
            }
        }
        return nbt;
    }
    
    public void deserializeNBT(CompoundTag nbt) {
        knockbackVec = MCUtil.nbtGetVec3d(nbt, "Vec");
        if (knockbackVec != null) {
            knockbackImpactStrength = nbt.getDouble("Power");
            minCos = nbt.getDouble("MinCos");
            hadImpactWithBlock = nbt.getBoolean("HadBlockImpact");
            explosionRadius = nbt.getFloat("ExplosionRadius");
            explosionDamage = nbt.getFloat("ExplosionDamage");
            syoPunchBaseDamage = nbt.getFloat("HamonPunchDmg");
            scarletOverdriveFireTicks = nbt.getInt("HamonFireTicks");
            hamonParticles = MCUtil.getNbtElement(nbt, "HamonSparks", StringTag.class)
                    .map(StringTag::getAsString).map(ResourceLocation::new)
                    .map(particleId -> {
                        if (ForgeRegistries.PARTICLE_TYPES.containsKey(particleId)) {
                            ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(particleId);
                            if (type instanceof SimpleParticleType) {
                                return (SimpleParticleType) type;
                            }
                        }
                        return null;
                    }).orElse(null);
        }
    }
    
    
    
    private void collideBoundingBox(Entity entity, Vec3 movementVec, boolean collideBlocks, boolean breakBlocks) {
        Level world = entity.level;
        if (world.isClientSide()) return;
        
        AABB aabb = entity.getBoundingBox().inflate(0.25);
        CollisionContext selectionContext = CollisionContext.of(entity);
        ServerLevel serverWorld = (ServerLevel) world;
        
        VoxelShape worldBorder = world.getWorldBorder().getCollisionShape();
        List<VoxelShape> worldBorderCollision = (
                Shapes.joinIsNotEmpty(worldBorder, Shapes.create(aabb.deflate(1.0E-7D)), BooleanOp.AND) ? Stream.<VoxelShape>empty() : Stream.of(worldBorder))
                .collect(Collectors.toList());
        
        List<Pair<Entity, VoxelShape>> potentialEntityCollisions = (getEntityCollisions(world, entity, aabb.expandTowards(movementVec), 
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(
                        e -> e.isPickable()
                        && (attackerStandUser == null || MCUtil.canHarm(attackerStandUser, e))
                        && !(entity instanceof LivingEntity && !MCUtil.canHarm((LivingEntity) entity, e))
                        ))).collect(Collectors.toList());
        Collection<Entity> entitiesCollided = new ArrayList<>();
        collideEntities(aabb, movementVec, world, 
                worldBorderCollision, potentialEntityCollisions, 
                selectionContext, entitiesCollided);

        if (!entitiesCollided.isEmpty()) {
            Vec3 vec = entity.getDeltaMovement();

            entitiesCollided.forEach(targetEntity -> {
                LivingEntity asLiving = targetEntity instanceof LivingEntity ? (LivingEntity) targetEntity : null;
                if (asLiving != null && syoPunchBaseDamage > 0) {
                    DamageUtil.dealHamonDamage(asLiving, syoPunchBaseDamage * 0.5f, entity, attacker, attack -> {
                        if (hamonParticles != null) {
                            attack.hamonParticle(hamonParticles);
                        }
                    });
                }
                if (scarletOverdriveFireTicks > 0) {
                    DamageUtil.dealDamageAndSetOnFire(targetEntity, 
                            e -> hurtTarget(e, ModDamageTypes.source(entity, "entityFlewInto"), 
                                    (float) getKnockbackImpactStrength() * 5), 
                            scarletOverdriveFireTicks / 20, false);
                }
                else {
                    hurtTarget(targetEntity, ModDamageTypes.source(entity, "entityFlewInto"), 
                            (float) getKnockbackImpactStrength() * 5);
                }
                if (asLiving != null) {
                    asLiving.knockback((float) getKnockbackImpactStrength(), -vec.x, -vec.z);
                }
            });
        }
        
        
        MutableBoolean doGlassBleeding = new MutableBoolean();
        float bleedingChance = asLiving != null ? BlockShardEntity.glassShardBleedingChance(asLiving) : 0;
        
        MutableFloat wallDamage = new MutableFloat(0);
        
        if (collideBlocks) {
            BlockCollisionResult collision = CollideBlocks.collideBoundingBox(movementVec, aabb, serverWorld, selectionContext);

			MutableFloat impactStrengthNew = new MutableFloat(knockbackImpactStrength);
            if (collision.blocks.size() > 0) {
                collision.blocks.stream()
                .distinct()
                .sorted(Comparator.comparingDouble(block -> {
                    AABB blockBB = block.getRight().bounds();
                    return MCUtil.getManhattanDist(blockBB, entity.getBoundingBox());
                }))
                .map(Pair::getLeft)
                .allMatch(blockPos -> {
                    BlockState blockState = world.getBlockState(blockPos);
                    float hardness = StandStatFormulas.getStandBreakBlockHardness(blockState, world, blockPos);
                    float useImpactStrength = 0;
                    if (hardness >= 0) {
                        useImpactStrength = hardness * 0.05f;
                    }
                    else if (hardness < 0) {
                        useImpactStrength = 1;
                    }
                    if (useImpactStrength > 0) {
                        setHadImpactWithBlock();
                        float impactLeft = (float) getKnockbackImpactStrength();
                        if (impactLeft < useImpactStrength) {
                            useImpactStrength = (impactLeft + useImpactStrength) / 2;
                        }
                        useImpactStrength = Math.min(impactLeft, useImpactStrength);
                        
                        float damage = useImpactStrength * 4;
                        wallDamage.add(damage);
                        
                        blockState.entityInside(serverWorld, blockPos, entity);
    
                        // episode #158 of me being on the spectrum
                        if (!doGlassBleeding.booleanValue() && asLiving != null 
                                && BlockShardEntity.isGlassBlock(blockState)
                                && asLiving.getRandom().nextFloat() < bleedingChance) {
                            doGlassBleeding.setTrue();
                        }
                        if (blockState.is(Blocks.CACTUS)) {
                            hurtTarget(entity, DamageSource.CACTUS, 1);
                        }
                        if (entity.isOnFire()) {
                            MCUtil.blockCatchFire(world, blockPos, blockState, null, asLiving);
                        }

						impactStrengthNew.setValue(impactStrengthNew.floatValue() - Math.max(useImpactStrength, 0.05f));
                    }

					return impactStrengthNew.floatValue() > 0;
                });
                
                Vec3 collisionDir = new Vec3(collision.movementX - collision.x, collision.movementY - collision.y, collision.movementZ - collision.z);
                Direction faceHit = Direction.getNearest(collisionDir.x, collisionDir.y, collisionDir.z);
                if (faceHit != Direction.DOWN) {
                    if (breakBlocks) {
                        if (explosionRadius > 0) {
                            AABB entityBB = entity.getBoundingBox();
                            Vec3 hitPos = new Vec3(
                                    Mth.lerp(faceHit.getStepX() * 0.5 + 0.5, entityBB.minX, entityBB.maxX), 
                                    Mth.lerp(faceHit.getStepY() * 0.5 + 0.5, entityBB.minY, entityBB.maxY), 
                                    Mth.lerp(faceHit.getStepZ() * 0.5 + 0.5, entityBB.minZ, entityBB.maxZ));
                            BlockPos hitBlockPos = BlockPos.containing(hitPos.add(Vec3.atBottomCenterOf(faceHit.getNormal()).scale(0.5)));
                            
                            HeavyPunchExplosion explosion = new HeavyPunchExplosion(world, attacker, new ActionTarget(hitBlockPos, faceHit.getOpposite()), 
                                    movementVec, explosionDmgSource, null, 
                                    hitPos.x, hitPos.y, hitPos.z, 
                                    explosionRadius, false, Explosion.BlockInteraction.DESTROY)
                                    .aoeDamage(explosionDamage)
                                    .entityNoDamage(entity);
                            if (CustomExplosion.explode(explosion)) {
                                this.blocksDestroyedByLastExplosion = explosion.getToBlow();
                                if (doGlassBleeding.booleanValue()) {
                                    BlockShardEntity.glassShardBleeding(asLiving);
                                }
                            }
                        }
                    }
                    
                    if (wallDamage.floatValue() > 0) {
                        hurtTarget(entity, DamageSource.FLY_INTO_WALL, wallDamage.floatValue());
                    }
                }

//				setKnockbackImpactStrength(impactStrengthNew.floatValue());
                reset();
            }
        }
        
    }
    
    private boolean hurtTarget(Entity target, DamageSource dmgSource, float amount) {
        boolean hurt = DamageUtil.hurtThroughInvulTicks(target, dmgSource, amount);
        if (attackerIsStand && attackerStandUser != null && target instanceof LivingEntity) {
            IStandPower.getStandPowerOptional(attackerStandUser).ifPresent(
                    attackerStand -> ResolveCounter.addResolve(attackerStand, (LivingEntity) target, amount));
        }
        return hurt;
    }
    
    
    private static Stream<Pair<Entity, VoxelShape>> getEntityCollisions(Level world, @Nullable Entity pEntity, AABB pArea, Predicate<Entity> pFilter) {
        if (pArea.getSize() < 1.0E-7D) {
            return Stream.empty();
        } else {
            AABB axisalignedbb = pArea.inflate(1.0E-7D);
            return world.getEntities(pEntity, axisalignedbb, pFilter.and(target -> {
                return target.isPickable();
            })).stream().map(entity -> Pair.of(entity, Shapes.create(entity.getBoundingBox())));
        }
    }
    
    private static void collideEntities(AABB aabb, Vec3 movementVec, Level world, 
            List<VoxelShape> worldBorderCollision, List<Pair<Entity, VoxelShape>> potentialEntityCollisions, 
            CollisionContext selectionContext, Collection<Entity> entityCollision) {
        double x = movementVec.x;
        double y = movementVec.y;
        double z = movementVec.z;
        
        if (y != 0) {
            y = collideEntitiesAxis(Direction.Axis.Y, aabb, world, y, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
            if (y != 0) {
                aabb = aabb.move(0, y, 0);
            }
        }

        boolean zFirst = Math.abs(x) < Math.abs(z);
        if (zFirst && z != 0) {
            z = collideEntitiesAxis(Direction.Axis.Z, aabb, world, z, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
//            if (z != 0) {
//                aabb = aabb.move(0, 0, z);
//            }
        }

        if (x != 0) {
            x = collideEntitiesAxis(Direction.Axis.X, aabb, world, x, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
//            if (!zFirst && x != 0) {
//                aabb = aabb.move(x, 0, 0);
//            }
        }

        if (!zFirst && z != 0) {
            z = collideEntitiesAxis(Direction.Axis.Z, aabb, world, z, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
        }
    }
    
    private static double collideEntitiesAxis(Direction.Axis movementAxis, AABB collisionBox, Level world, double desiredOffset, 
            List<VoxelShape> worldBorderCollision, List<Pair<Entity, VoxelShape>> potentialEntityCollisions, 
            CollisionContext pSelectionContext, Collection<Entity> entityCollision) {
        if (!(collisionBox.getXsize() < 1.0E-6D) && !(collisionBox.getYsize() < 1.0E-6D) && !(collisionBox.getZsize() < 1.0E-6D)) {
            if (Math.abs(desiredOffset) < 1.0E-7D) {
                return 0;
            } else {
                AxisCycle pRotationAxis = AxisCycle.between(movementAxis, Direction.Axis.Z);
                AxisCycle axisrotation = pRotationAxis.inverse();
                Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);

                MutableDouble worldBorderCollideOffset = new MutableDouble(desiredOffset);
                worldBorderCollision.stream().forEach(voxelShape -> {
                    worldBorderCollideOffset.setValue(voxelShape.collide(direction$axis2, collisionBox, worldBorderCollideOffset.doubleValue()));
                });
                desiredOffset = worldBorderCollideOffset.doubleValue();
                
                double maxOffset = desiredOffset;
                MutableDouble collidedOffset = new MutableDouble(maxOffset);
                potentialEntityCollisions.stream().forEach(entityVoxelShape -> {
                    double entityCollideResult = entityVoxelShape.getRight().collide(direction$axis2, collisionBox, collidedOffset.doubleValue());
                    if (entityCollideResult != maxOffset) {
                        entityCollision.add(entityVoxelShape.getLeft());
                        collidedOffset.setValue(entityCollideResult);
                    }
                });
                
                return desiredOffset;
            }
        } else {
            return desiredOffset;
        }
    }
    
    
    public static boolean isSoftMaterial(BlockState blockState) {
        return MCUtil.isSoftMaterial(blockState);
    }
    
    
    
    public static Optional<KnockbackCollisionImpact> getHandler(Entity entity) {
        return entity.getCapability(EntityUtilCapProvider.CAPABILITY)
                .resolve().map(EntityUtilCap::getKbImpact);
    }
}