package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.criterion.predicate.ActionPredicate;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class ActionPerformTrigger extends SimpleCriterionTrigger<ActionPerformTrigger.Instance> {
    private final ResourceLocation id;

    public ActionPerformTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, Action<?> action) {
        trigger(player, criterion -> criterion.matches(action));
    }

    @Override
    protected ActionPerformTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        return new ActionPerformTrigger.Instance(id, playerPredicate, ActionPredicate.fromJson(json.get("action")));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ActionPredicate actionPredicate;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, ActionPredicate action) {
            super(criterion, player);
            this.actionPredicate = action;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("action", actionPredicate.serializeToJson());
            return jsonobject;
        }

        private boolean matches(Action<?> action) {
            return actionPredicate.matches(action);
        }
    }

}
