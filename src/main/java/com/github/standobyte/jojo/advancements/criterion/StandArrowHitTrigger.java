package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.StandArrowHitPredicate;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.advancements.critereon.ContextAwarePredicate;

public class StandArrowHitTrigger extends SimpleCriterionTrigger<StandArrowHitTrigger.Instance> {
    private final ResourceLocation id;

    public StandArrowHitTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayer player, LivingEntity target, boolean gaveStand) {
        LootContext targetCtx = EntityPredicate.createContext(player, target);
        IStandPower targetStand = IStandPower.getStandPowerOptional(target).orElse(null);
        trigger(player, (criterion) -> {
            return criterion.matches(player, targetCtx, gaveStand, targetStand, player.is(target));
        });
    }

    @Override
    public StandArrowHitTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate, DeserializationContext conditionArrayParser) {
        JsonElement targetElem = json.get("target");
        return new StandArrowHitTrigger.Instance(
                this.id, 
                playerPredicate, 
                targetElem != null
                        ? ContextAwarePredicate.fromElement("target", conditionArrayParser, targetElem, net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY)
                        : EntityPredicate.wrap(EntityPredicate.ANY), 
                StandArrowHitPredicate.fromJson(json.get("arrow_hit")));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate targetPredicate;
        private final StandArrowHitPredicate standArrowHitPredicate;

        public Instance(ResourceLocation id, ContextAwarePredicate player, 
                ContextAwarePredicate targetPredicate, StandArrowHitPredicate standArrowHitPredicate) {
            super(id, player);
            this.targetPredicate = targetPredicate;
            this.standArrowHitPredicate = standArrowHitPredicate;
        }

        public boolean matches(ServerPlayer player, LootContext targetCtx, 
                boolean gaveStand, IStandPower targetStand, boolean shotSelf) {
            return targetPredicate.matches(targetCtx) && standArrowHitPredicate.matches(player, gaveStand, targetStand, shotSelf);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("target", this.targetPredicate.toJson(serializer));
            jsonobject.add("arrow_hit", this.standArrowHitPredicate.serializeToJson());
            return jsonobject;
        }
    }
}
