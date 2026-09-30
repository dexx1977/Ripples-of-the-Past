package com.github.standobyte.jojo.advancements.criterion;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public class RPSGameTrigger extends SimpleCriterionTrigger<RPSGameTrigger.Instance> {
    private final ResourceLocation id;

    public RPSGameTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }
    
    public void trigger(ServerPlayer player, RockPaperScissorsGame game, boolean standTaken) {
        trigger(player, criterion -> criterion.matches(player, game, standTaken));
    }

    @Override
    protected RPSGameTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        return new RPSGameTrigger.Instance(id, playerPredicate, 
                json.has("won_game") ? GsonHelper.getAsBoolean(json, "won_game") : null, 
                json.has("stand_taken") ? GsonHelper.getAsBoolean(json, "stand_taken") : null);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Boolean gameWon;
        @Nullable
        private final Boolean standTaken;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, 
                Boolean gameWon, Boolean standTaken) {
            super(criterion, player);
            this.gameWon = gameWon;
            this.standTaken = standTaken;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            if (gameWon != null) jsonobject.addProperty("won_game", gameWon);
            if (standTaken != null) jsonobject.addProperty("stand_taken", standTaken);
            return jsonobject;
        }

        private boolean matches(ServerPlayer player, RockPaperScissorsGame game, boolean standTaken) {
            return (this.gameWon == null || this.gameWon.booleanValue() == player.getUUID().equals(game.getWinner()))
                    && (this.standTaken == null || this.standTaken.booleanValue() == standTaken);
        }
    }
}
