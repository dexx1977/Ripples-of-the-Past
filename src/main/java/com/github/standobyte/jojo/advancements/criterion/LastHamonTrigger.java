package com.github.standobyte.jojo.advancements.criterion;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.resources.ResourceLocation;

public class LastHamonTrigger extends SimpleCriterionTrigger<LastHamonTrigger.Instance> {
    private final ResourceLocation id;

    public LastHamonTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, @Nullable Entity hamonSource) {
        LootContext sourceCtx = EntityPredicate.createContext(player, hamonSource);
        trigger(player, criterion -> criterion.matches(sourceCtx));
    }

    @Override
    protected LastHamonTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        // 1.20.1 moved the json parsing of entity predicates onto EntityPredicate
        ContextAwarePredicate sourcePredicate = net.minecraft.advancements.critereon.EntityPredicate
                .fromJson(json, "source", conditionArrayParser);
        return new LastHamonTrigger.Instance(id, playerPredicate, sourcePredicate);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate hamonSource;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, ContextAwarePredicate hamonSource) {
            super(criterion, player);
            this.hamonSource = hamonSource;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("source", hamonSource.toJson(serializer));
            return jsonobject;
        }

        private boolean matches(LootContext sourceCtx) {
            return hamonSource.matches(sourceCtx);
        }
    }

}
