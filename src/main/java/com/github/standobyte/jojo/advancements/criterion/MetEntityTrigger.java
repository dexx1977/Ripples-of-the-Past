package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.advancements.critereon.ContextAwarePredicate;

public class MetEntityTrigger extends SimpleCriterionTrigger<MetEntityTrigger.Instance> {
    private final ResourceLocation id;

    public MetEntityTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }
    
    public void trigger(ServerPlayer player, Entity pEntity) {
        LootContext lootContext = EntityPredicate.createContext(player, pEntity);
        this.trigger(player, instance -> {
            return instance.matches(lootContext);
        });
    }

    @Override
    public MetEntityTrigger.Instance createInstance(JsonObject json, 
            ContextAwarePredicate playerPredicate, DeserializationContext conditionsParser) {
        ContextAwarePredicate entityPredicate = ContextAwarePredicate.fromElement("entity", conditionsParser, json.get("entity"), net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
        return new MetEntityTrigger.Instance(id, playerPredicate, entityPredicate);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate entity;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, ContextAwarePredicate entity) {
            super(criterion, player);
            this.entity = entity;
        }

        public static SummonedEntityTrigger.Instance metEntity(EntityPredicate.Builder entityBuilder) {
            return new SummonedEntityTrigger.Instance(ContextAwarePredicate.ANY, ContextAwarePredicate.wrap(entityBuilder.build()));
        }
        
        public boolean matches(LootContext pLootContext) {
            return this.entity.matches(pLootContext);
        }
        
        @Override
        public JsonObject serializeToJson(SerializationContext pConditions) {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("entity", this.entity.toJson(pConditions));
            return jsonobject;
        }
    }
}
