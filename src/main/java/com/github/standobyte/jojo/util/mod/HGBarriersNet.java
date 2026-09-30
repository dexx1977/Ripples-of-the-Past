package com.github.standobyte.jojo.util.mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.GraphAdjacencyList;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class HGBarriersNet {
    private Map<HGBarrierEntity, ShootingPoints> placedBarriers = new HashMap<>();
    private GraphAdjacencyList<Vec3> closePoints = new GraphAdjacencyList<>();
    private double lastShotGap = -1;
    private boolean canShoot;

    public void tick() {
        Iterator<Map.Entry<HGBarrierEntity, ShootingPoints>> iter = placedBarriers.entrySet().iterator();
        while (iter.hasNext()) {
            HGBarrierEntity barrier = iter.next().getKey();
            if (!barrier.isAlive() || barrier.wasRipped()) {
                onRemoved(barrier);
                iter.remove();
            }
        }
        canShoot = true;
    }
    
    public void add(HGBarrierEntity barrier) {
        placedBarriers.put(barrier, generateShootingPoints(barrier));
        onUpdate();
    }
    
    private static final double SHOOTING_POINTS_GAP = 8;
    private ShootingPoints generateShootingPoints(HGBarrierEntity entity) {
        Vec3 posA = entity.position();
        Vec3 posB = entity.getOriginPoint(1.0F);
        Vec3 vecAToB = posB.subtract(posA);
        
        List<Vec3> shootingPoints = new ArrayList<>();
        if (vecAToB.lengthSqr() <= SHOOTING_POINTS_GAP * SHOOTING_POINTS_GAP * 4) {
            shootingPoints.add(posA.add(vecAToB.scale(0.5)));
        }
        else {
            int steps = Mth.floor(vecAToB.length() / SHOOTING_POINTS_GAP);
            Vec3 nextPoint = posA;
            Vec3 stepVec = vecAToB.normalize().scale(SHOOTING_POINTS_GAP);
            for (int i = 0; i < steps; i++) {
                nextPoint = nextPoint.add(stepVec);
                shootingPoints.add(nextPoint);
            }
        }

        return new ShootingPoints(shootingPoints);
    }
    
    private void onRemoved(HGBarrierEntity barrier) {
        
        onUpdate();
    }
    
    private void onUpdate() {
        if (lastShotGap > -1) {
            closePoints.clear();
            lastShotGap = -1;
        }
    }
    
    public int getSize() {
        return placedBarriers.size();
    }

    public void shootEmeraldsFromBarriers(IStandPower standPower, HierophantGreenEntity stand, 
            Vec3 targetPos, int tick, double maxEmeralds, float staminaPerEmerald, double minGap, boolean breakBlocks) {
        if (!canShoot) return;
        List<Vec3> shootingPoints = placedBarriers.values().stream().flatMap(points -> 
        points.shootingPoints.stream()).collect(Collectors.toCollection(LinkedList::new));
        if (lastShotGap != minGap) {
            closePoints.create((pointA, pointB) -> pointA.distanceToSqr(pointB) < minGap * minGap, shootingPoints);
            lastShotGap = minGap;
        }

        Set<Vec3> pointsToShootThisTick = new HashSet<>();
        GeneralUtil.doFractionTimes(() -> {
            Vec3 point = shootingPoints.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(targetPos))).get();
            pointsToShootThisTick.add(point);
            shootingPoints.remove(point);
            closePoints.getAllAdjacent(point).forEach(closePoint -> shootingPoints.remove(closePoint));
        }, maxEmeralds, () -> shootingPoints.isEmpty());
        
        for (Vec3 point : pointsToShootThisTick) {
            if (!standPower.consumeStamina(staminaPerEmerald)) {
                break;
            }
            GeneralUtil.doFractionTimes(() -> shootEmerald(stand, point, targetPos, tick == 0, breakBlocks), 
                    StandStatFormulas.projectileFireRateScaling(stand, standPower));
        }
        canShoot = false;
    }
    
    public Stream<Vec3> wasRippedAt() {
        return placedBarriers.keySet().stream()
                .flatMap(barrier -> barrier.wasRippedAt().map(point -> Stream.of(point)).orElse(Stream.empty()));
    }
    
    public enum PointsChoice {
        RANDOM,
        CLOSEST
    }
    
    private void shootEmerald(HierophantGreenEntity stand, Vec3 shootingPos, Vec3 targetPos, boolean playSound, boolean breakBlocks) {
        if (!stand.level.isClientSide()) {
            HGEmeraldEntity emeraldEntity = new HGEmeraldEntity(stand, stand.level, null);
            emeraldEntity.setPos(shootingPos.x, shootingPos.y, shootingPos.z);
            emeraldEntity.setBreakBlocks(breakBlocks);
            emeraldEntity.setLowerKnockback(true);
            Vec3 shootVec = targetPos.subtract(shootingPos);
            emeraldEntity.shoot(shootVec.x, shootVec.y, shootVec.z, 1.5F, stand.getProjectileInaccuracy(2.0F));
            emeraldEntity.setDamageFactor(0.75F);
            emeraldEntity.withStandSkin(stand.getStandSkin());
            stand.addProjectile(emeraldEntity);
            if (playSound) {
                MCUtil.playSound(stand.level, null, shootingPos.x, shootingPos.y, shootingPos.z, 
                        ModSounds.HIEROPHANT_GREEN_EMERALD_SPLASH.get(), 
                        stand.getSoundSource(), 1.0F, 1.0F, StandUtil::playerCanHearStands);
            }
        }
    }
    
    public void setStandSkin(Optional<ResourceLocation> standSkin) {
        placedBarriers.keySet().forEach(barrier -> barrier.withStandSkin(standSkin));
    }
    
    private class ShootingPoints {
        private final List<Vec3> shootingPoints;
        
        private ShootingPoints(List<Vec3> shootingPoints) {
            this.shootingPoints = shootingPoints;
        }
    }
}
