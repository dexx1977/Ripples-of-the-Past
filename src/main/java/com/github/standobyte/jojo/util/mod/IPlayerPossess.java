package com.github.standobyte.jojo.util.mod;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import com.github.standobyte.jojo.init.power.RegistryEntry;

public interface IPlayerPossess {
    void jojoPossessEntity(@Nullable Entity entity, boolean asAlive, @Nullable RegistryEntry<?> context);
    @Nullable Entity jojoGetPossessedEntity();
    boolean jojoIsPossessingAsAlive();
    Optional<GameType> jojoGetPrePossessGameMode();
    void jojoSetPrePossessGameMode(Optional<GameType> gameType);
    @Nullable RegistryEntry<?> jojoGetPossessionContext();
    void jojoOnPossessingDead();
    
    public static Entity getPossessedEntity(Entity possessing) {
        return possessing instanceof IPlayerPossess ? ((IPlayerPossess) possessing).jojoGetPossessedEntity() : null;
    }
}
