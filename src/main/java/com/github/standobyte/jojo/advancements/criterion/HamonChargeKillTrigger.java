package com.github.standobyte.jojo.advancements.criterion;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraft.advancements.critereon.ContextAwarePredicate;

public class HamonChargeKillTrigger extends SimpleCriterionTrigger<HamonChargeKillTrigger.Instance> {
    private final ResourceLocation id;

    public HamonChargeKillTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayer player, LivingEntity killed, @Nullable Entity chargedEntity, @Nullable BlockPos chargedBlockPos) {
        LootContext killedLootCtx = EntityPredicate.createContext(player, killed);
        if (chargedEntity != null) {
            LootContext chargedLootCtx = EntityPredicate.createContext(player, chargedEntity);
            trigger(player, (criterion) -> {
               return criterion.matches(killed, killedLootCtx, chargedLootCtx);
            });
        }
        else if (chargedBlockPos != null) {
            BlockState blockState = player.level().getBlockState(chargedBlockPos);
            trigger(player, (criterion) -> {
               return criterion.matches(killed, killedLootCtx, blockState, chargedBlockPos, player.serverLevel());
            });
        }
    }

    @Override
    public HamonChargeKillTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate playerPredicate, DeserializationContext conditionArrayParser) {
        Block block = deserializeBlock(json, "block");
        StatePropertiesPredicate blockState = StatePropertiesPredicate.fromJson(json.get("state"));
        if (block != null) {
            blockState.checkState(block.getStateDefinition(), (property) -> {
                throw new JsonSyntaxException("Block " + block + " has no property " + property + ":");
            });
        }

        JsonElement killedEntityElem = json.get("killed_entity");
        JsonElement chargedEntityElem = json.get("charged_entity");
        return new HamonChargeKillTrigger.Instance(
                id, 
                playerPredicate, 
                killedEntityElem != null
                        ? ContextAwarePredicate.fromElement("killed_entity", conditionArrayParser, killedEntityElem, net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY)
                        : EntityPredicate.wrap(EntityPredicate.ANY), 
                PowerPredicate.fromJson(json.get("killed_power"), null),
                chargedEntityElem != null
                        ? ContextAwarePredicate.fromElement("charged_entity", conditionArrayParser, chargedEntityElem, net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY)
                        : EntityPredicate.wrap(EntityPredicate.ANY), 
                block, 
                blockState,
                LocationPredicate.fromJson(json.get("location")));
    }

    @Nullable
    private static Block deserializeBlock(JsonObject json, String key) {
        if (json.has(key)) {
            ResourceLocation resLoc = new ResourceLocation(GsonHelper.getAsString(json, key));
            return Optional.ofNullable(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getRaw(resLoc)).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown block type '" + resLoc + "'");
            });
        } else {
            return null;
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate killedPredicate;
        private final PowerPredicate killedPowerPredicate;
        
        private final ContextAwarePredicate chargedPredicate;
        
        private final Block chargedBlock;
        private final StatePropertiesPredicate chargedBlockState;
        private final LocationPredicate chargedBlockLocation;

        public Instance(ResourceLocation id, ContextAwarePredicate player, 
                ContextAwarePredicate killedPredicate, PowerPredicate killedPowerPredicate, 
                ContextAwarePredicate chargedPredicate, 
                Block chargedBlock, StatePropertiesPredicate chargedBlockState, LocationPredicate chargedBlockLocation) {
            super(id, player);
            this.killedPredicate = killedPredicate;
            this.killedPowerPredicate = killedPowerPredicate;
            this.chargedPredicate = chargedPredicate;
            this.chargedBlock = chargedBlock;
            this.chargedBlockState = chargedBlockState;
            this.chargedBlockLocation = chargedBlockLocation;
        }

        public boolean matches(LivingEntity killed, LootContext killedCtx, LootContext chargedCtx) {
            return this.chargedBlock == null && chargedBlockState == StatePropertiesPredicate.ANY && chargedBlockLocation == LocationPredicate.ANY && 
                    this.killedPredicate.matches(killedCtx) && killedPowerPredicate.matches(killed) && 
                    this.chargedPredicate.matches(chargedCtx);
        }

        public boolean matches(LivingEntity killed, LootContext killedCtx, BlockState blockState, BlockPos blockPos, ServerLevel serverWorld) {
            return this.chargedPredicate == ContextAwarePredicate.ANY && 
                    this.killedPredicate.matches(killedCtx) && killedPowerPredicate.matches(killed) && 
                    (this.chargedBlock == null || blockState.is(this.chargedBlock)) && 
                    this.chargedBlockState.matches(blockState) && 
                    this.chargedBlockLocation.matches(serverWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("killed_entity", killedPredicate.toJson(serializer));
            jsonobject.add("killed_power", killedPowerPredicate.serializeToJson());
            
            jsonobject.add("charged_entity", chargedPredicate.toJson(serializer));
            
            if (chargedBlock != null) {
                jsonobject.addProperty("block", ForgeRegistries.BLOCKS.getKey(chargedBlock).toString());
            }
            jsonobject.add("state", chargedBlockState.serializeToJson());
            jsonobject.add("location", chargedBlockLocation.serializeToJson());
            return jsonobject;
        }
    }
}
