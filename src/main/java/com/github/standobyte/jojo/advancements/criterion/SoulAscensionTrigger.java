package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class SoulAscensionTrigger extends SimpleCriterionTrigger<SoulAscensionTrigger.Instance> {
    private final ResourceLocation id;

    public SoulAscensionTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayer player, IStandPower stand, int ascensionTicks) {
        trigger(player, criterion -> criterion.matches(stand, ascensionTicks));
    }

    @Override
    protected SoulAscensionTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        return new SoulAscensionTrigger.Instance(id, playerPredicate, 
                PowerPredicate.fromJson(json.get("stand"), null), MinMaxBounds.Ints.fromJson(json.get("ascension_ticks")));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private PowerPredicate standPower;
        private MinMaxBounds.Ints ascensionTicks;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player, 
                PowerPredicate standPower, MinMaxBounds.Ints ascensionTicks) {
            super(criterion, player);
            this.standPower = standPower;
            this.ascensionTicks = ascensionTicks;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("stand", standPower.serializeToJson());
            jsonobject.add("ascension_ticks", ascensionTicks.serializeToJson());
            return jsonobject;
        }

        private boolean matches(IStandPower stand, int ascensionTicks) {
            return this.standPower.matchesPower(PowerClassification.STAND, stand) && this.ascensionTicks.matches(ascensionTicks);
        }
    }

}
