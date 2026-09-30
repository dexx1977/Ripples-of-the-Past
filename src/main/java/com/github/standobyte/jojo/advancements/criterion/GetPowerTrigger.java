package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class GetPowerTrigger extends SimpleCriterionTrigger<GetPowerTrigger.Instance> {
    private final ResourceLocation id;

    public GetPowerTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, PowerClassification classification, IPower<?, ?> power) {
        trigger(player, criterion -> criterion.matches(classification, power));
    }

    @Override
    protected GetPowerTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        return new GetPowerTrigger.Instance(id, playerPredicate, PowerPredicate.fromJson(json.get("jojopower"), null));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final PowerPredicate powerPredicate;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, PowerPredicate powerPredicate) {
            super(criterion, player);
            this.powerPredicate = powerPredicate;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("jojopower", this.powerPredicate.serializeToJson());
            return jsonobject;
        }

        private boolean matches(PowerClassification classification, IPower<?, ?> power) {
            return this.powerPredicate.matchesPower(classification, power);
        }
    }

}
