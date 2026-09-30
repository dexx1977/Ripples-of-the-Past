package com.github.standobyte.jojo.init;

import java.util.Optional;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModDataSerializers {
    // 1.20.1 registers entity data serializers in the vanilla registry itself
    // (Forge's DataSerializerEntry wrapper is gone)
    public static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZERS = 
            DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, JojoMod.MOD_ID);
    
    public static final RegistryObject<EntityDataSerializer<Optional<StandEntityTask>>> STAND_ENTITY_TASK = 
            DATA_SERIALIZERS.register("stand_action", StandEntityTask.SERIALIZER);
    
    public static final RegistryObject<EntityDataSerializer<Optional<Vec3>>> OPTIONAL_VECTOR3D = DATA_SERIALIZERS.register("optional_vector3d", 
            () -> new EntityDataSerializer<Optional<Vec3>>() {

        @Override
        public void write(FriendlyByteBuf buf, Optional<Vec3> value) {
            NetworkUtil.writeOptional(buf, value, vector -> {
                buf.writeDouble(vector.x);
                buf.writeDouble(vector.y);
                buf.writeDouble(vector.z);
            });
        }

        @Override
        public Optional<Vec3> read(FriendlyByteBuf buf) {
            return NetworkUtil.readOptional(buf, () -> {
                Vec3 vec = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
                return vec;
            });
        }

        @Override
        public Optional<Vec3> copy(Optional<Vec3> value) {
            return value;
        }
    });
    
    public static final RegistryObject<EntityDataSerializer<Optional<ResourceLocation>>> OPTIONAL_RES_LOC = DATA_SERIALIZERS.register("optional_res_loc", 
            () -> new EntityDataSerializer<Optional<ResourceLocation>>() {

        @Override
        public void write(FriendlyByteBuf buf, Optional<ResourceLocation> value) {
            NetworkUtil.writeOptional(buf, value, buf::writeResourceLocation);
        }

        @Override
        public Optional<ResourceLocation> read(FriendlyByteBuf buf) {
            return NetworkUtil.readOptional(buf, FriendlyByteBuf::readResourceLocation);
        }

        @Override
        public Optional<ResourceLocation> copy(Optional<ResourceLocation> value) {
            return value;
        }
    });
}

