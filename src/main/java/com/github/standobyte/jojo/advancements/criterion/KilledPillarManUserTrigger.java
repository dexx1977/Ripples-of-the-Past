package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.PillarmanStagePredicate;
import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.advancements.critereon.ContextAwarePredicate;

public class KilledPillarManUserTrigger extends SimpleCriterionTrigger<KilledPillarManUserTrigger.Instance> {
    private final ResourceLocation id;
    private final boolean isPlayerKilled;

    public KilledPillarManUserTrigger(ResourceLocation id, boolean isPlayerKilled) {
        this.id = id;
        this.isPlayerKilled = isPlayerKilled;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayer player, Entity entity, DamageSource damageSource) {
        if (entity != null) {
            LootContext lootCtx = EntityPredicate.createContext(player, entity);
            LivingEntity livingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            trigger(player, (criterion) -> {
                return criterion.matches(player, lootCtx, damageSource, 
                        isPlayerKilled ? livingEntity : player, 
                        isPlayerKilled ? player : livingEntity);
            });
        }
    }

    @Override
    public KilledPillarManUserTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate, DeserializationContext conditionArrayParser) {
        JsonElement entityElem = json.get("entity");
        return new KilledPillarManUserTrigger.Instance(
                this.id, 
                playerPredicate, 
                entityElem != null
                        ? ContextAwarePredicate.fromElement("entity", conditionArrayParser, entityElem, net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY)
                        : EntityPredicate.wrap(EntityPredicate.ANY), 
                DamageSourcePredicate.fromJson(json.get("killing_blow")), 
                PowerPredicate.fromJson(json.get("power"), null),
                PowerPredicate.fromJson(json.get("killed_power"), null),
                PillarmanStagePredicate.fromJson(json.get("pillarman_stage")));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate entityPredicate;
        private final DamageSourcePredicate killingBlow;
        private final PowerPredicate powerPredicate;
        private final PowerPredicate killedPowerPredicate;
        private final PillarmanStagePredicate pillarmanStagePredicate;

        public Instance(ResourceLocation id, ContextAwarePredicate player, 
                ContextAwarePredicate entityPredicate, DamageSourcePredicate killingBlow, 
                PowerPredicate powerPredicate, PowerPredicate killedPowerPredicate,
                PillarmanStagePredicate pillarmanStagePredicate) {
            super(id, player);
            this.entityPredicate = entityPredicate;
            this.killingBlow = killingBlow;
            this.powerPredicate = powerPredicate;
            this.killedPowerPredicate = killedPowerPredicate;
            this.pillarmanStagePredicate = pillarmanStagePredicate;
        }

        public boolean matches(ServerPlayer player, LootContext lootCtx, DamageSource damageSource, 
                LivingEntity entity, LivingEntity killed) {
            return this.killingBlow.matches(player, damageSource) && this.entityPredicate.matches(lootCtx)
                    && powerPredicate.matches(entity) && killedPowerPredicate.matches(killed)
                    && pillarmanStagePredicate.matches(killed);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("entity", this.entityPredicate.toJson(serializer));
            jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
            jsonobject.add("power", this.powerPredicate.serializeToJson());
            jsonobject.add("killed_power", this.killedPowerPredicate.serializeToJson());
            jsonobject.add("pillarman_stage", this.pillarmanStagePredicate.serializeToJson());
            return jsonobject;
        }
    }
}