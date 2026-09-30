package com.github.standobyte.jojo.util.mc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;

public abstract class PlayerStatListener<T> {
    private final ServerPlayer player;
    private final Stat<T> stat;
    
    private boolean firstTick = true;
    private long oldVal;
    
    public PlayerStatListener(Stat<T> stat, ServerPlayer player) {
        this.stat = stat;
        this.player = player;
    }
    
    public void tick() {
        long newVal = player.getStats().getValue(stat);
        if (firstTick) {
            firstTick = false;
            oldVal = newVal;
        }
        else {
            if (newVal != oldVal) {
                handleChanged(stat.getValue(), player, oldVal, newVal);
                oldVal = newVal;
            }
        }
    }
    
    protected abstract void handleChanged(T item, ServerPlayer player, long oldVal, long newVal);
}
