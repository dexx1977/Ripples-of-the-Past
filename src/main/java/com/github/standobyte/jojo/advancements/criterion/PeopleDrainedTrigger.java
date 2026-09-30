package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class PeopleDrainedTrigger extends SimpleCriterionTrigger<PeopleDrainedTrigger.Instance> {
    private final ResourceLocation id;

    public PeopleDrainedTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, int peopleDrained, int zombiesCreated) {
        trigger(player, criterion -> criterion.matches(peopleDrained, zombiesCreated));
    }

    @Override
    protected PeopleDrainedTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        return new PeopleDrainedTrigger.Instance(id, playerPredicate, 
                MinMaxBounds.Ints.fromJson(json.get("people_drained")), 
                MinMaxBounds.Ints.fromJson(json.get("zombies_created")));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private MinMaxBounds.Ints peopleDrained;
        private MinMaxBounds.Ints zombiesCreated;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, 
                MinMaxBounds.Ints peopleDrained, MinMaxBounds.Ints zombiesCreated) {
            super(criterion, player);
            this.peopleDrained = peopleDrained;
            this.zombiesCreated = zombiesCreated;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("people_drained", peopleDrained.serializeToJson());
            jsonobject.add("zombies_created", zombiesCreated.serializeToJson());
            return jsonobject;
        }

        private boolean matches(int peopleDrained, int zombiesCreated) {
            return this.peopleDrained.matches(peopleDrained) && this.zombiesCreated.matches(zombiesCreated);
        }
    }

}
