package com.github.standobyte.jojo.capability.world;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import com.github.standobyte.jojo.util.TreeLeavesDecay;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;

public class WorldUtilCap {
    private final Level world;
    final TimeStopHandler timeStops;
    private final Queue<ThrownEgg> chargedEggs = new LinkedList<>();
    private final List<TreeLeavesDecay> decayingTrees = new LinkedList<>();
//    public final Map<UUID, HamonProjectileShieldEntity> projectileShields = new HashMap<>();
    
    public WorldUtilCap(Level world) {
        this.world = world;
        this.timeStops = new TimeStopHandler(world);
    }
    
    public void tick() {
        timeStops.tick();
        if (!world.isClientSide()) {
            tickEggsQueue();
            tickGETreesDecay();
        }
    }
    
    
    public TimeStopHandler getTimeStopHandler() {
        return timeStops;
    }
    
    
    public void addChargedEggEntity(ThrownEgg entity) {
        chargedEggs.add(entity);
    }
    
    public Optional<ThrownEgg> eggChargingChicken(Entity chicken) {
        if (chargedEggs.isEmpty()) {
            return Optional.empty();
        }
        
        return chargedEggs.stream()
                .filter(egg -> egg.getBoundingBox().intersects(chicken.getBoundingBox()))
                .findFirst();
    }
    
    private void tickEggsQueue() {
        Iterator<ThrownEgg> it = chargedEggs.iterator();
        while (it.hasNext()) {
            ThrownEgg entity = it.next();
            if (!entity.isAlive()) {
                it.remove();
            }
        }
    }
    
    
    public void addDecayingTree(TreeLeavesDecay tree) {
        decayingTrees.add(tree);
    }
    
    private void tickGETreesDecay() {
        Iterator<TreeLeavesDecay> iter = decayingTrees.iterator();
        while (iter.hasNext()) {
            TreeLeavesDecay tree = iter.next();
            if (tree.tick(world)) {
                iter.remove();
            }
        }
    }
}
