package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.HamonStatsPredicate;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class HamonStatsTrigger extends SimpleCriterionTrigger<HamonStatsTrigger.Instance> {
    private final ResourceLocation id;

    public HamonStatsTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, int strengthLevel, int controlLevel, float breathingTrainingLevel) {
        trigger(player, criterion -> criterion.matches(strengthLevel, controlLevel, breathingTrainingLevel));
    }

    @Override
    protected HamonStatsTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        HamonStatsPredicate statsPredicate = HamonStatsPredicate.fromJson(json.get("hamon_stats"));
        return new HamonStatsTrigger.Instance(id, playerPredicate, statsPredicate);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final HamonStatsPredicate statsPredicate;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, HamonStatsPredicate statsPredicate) {
            super(criterion, player);
            this.statsPredicate = statsPredicate;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("hamon_stats", statsPredicate.serializeToJson());
            return jsonobject;
        }

        private boolean matches(int strengthLevel, int controlLevel, float breathingTrainingLevel) {
            return this.statsPredicate.matches(strengthLevel, controlLevel, breathingTrainingLevel);
        }
    }

}
