package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Trigger fired by the mod when a player stands inside one of the mod's
 * generated structures. Replaces the vanilla {@code minecraft:location}
 * criterion trigger, which was removed in 1.20.1 (the mod's structure
 * advancements written with it were being granted unconditionally).
 */
public class StandStructureTrigger extends SimpleCriterionTrigger<StandStructureTrigger.Instance> {
    private final ResourceLocation id;

    public StandStructureTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayer player, ResourceKey<Structure> structureKey) {
        trigger(player, instance -> instance.matches(player, structureKey));
    }

    @Override
    protected StandStructureTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate,
            DeserializationContext conditionArrayParser) {
        ResourceLocation structureId = new ResourceLocation(GsonHelper.getAsString(json, "structure"));
        ResourceLocation biomeId = json.has("biome") ? new ResourceLocation(GsonHelper.getAsString(json, "biome")) : null;
        return new StandStructureTrigger.Instance(id, playerPredicate, structureId, biomeId);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation structureId;
        private final ResourceLocation biomeId;

        public Instance(ResourceLocation criterion, ContextAwarePredicate player,
                ResourceLocation structureId, ResourceLocation biomeId) {
            super(criterion, player);
            this.structureId = structureId;
            this.biomeId = biomeId;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.addProperty("structure", this.structureId.toString());
            if (this.biomeId != null) {
                jsonobject.addProperty("biome", this.biomeId.toString());
            }
            return jsonobject;
        }

        private boolean matches(ServerPlayer player, ResourceKey<Structure> structureKey) {
            if (!structureKey.location().equals(this.structureId)) {
                return false;
            }
            ServerLevel level = player.serverLevel();
            if (!level.isLoaded(player.blockPosition())) {
                return false;
            }
            StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), structureKey);
            if (!start.isValid()) {
                return false;
            }
            if (this.biomeId != null) {
                Holder<Biome> biome = level.getBiome(player.blockPosition());
                return biome.is(this.biomeId);
            }
            return true;
        }
    }
}
