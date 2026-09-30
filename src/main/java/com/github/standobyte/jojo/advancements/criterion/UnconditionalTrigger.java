package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

public class UnconditionalTrigger extends SimpleCriterionTrigger<UnconditionalTrigger.Instance> {
    private final ResourceLocation id;

    public UnconditionalTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, (criterion) -> {
            return true;
        });
    }

    @Override
    public UnconditionalTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        return new UnconditionalTrigger.Instance(id, playerPredicate);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ResourceLocation criterion, ContextAwarePredicate player) {
            super(criterion, player);
        }
    }
}
