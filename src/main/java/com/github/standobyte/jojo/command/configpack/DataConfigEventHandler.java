package com.github.standobyte.jojo.command.configpack;

import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DataConfigEventHandler<T extends SimpleJsonResourceReloadListener & IDataConfig> {
    private final T dataConfig;
    
    public static <T extends SimpleJsonResourceReloadListener & IDataConfig> void registerEventHandler(T dataConfig, IEventBus forgeEventBus) {
        DataConfigEventHandler<T> eventHandler = new DataConfigEventHandler<>(dataConfig);
        forgeEventBus.register(eventHandler);
    }
    
    private DataConfigEventHandler(T dataConfig) {
        this.dataConfig = dataConfig;
    }
    
    @SubscribeEvent
    public void onDataPackLoad(AddReloadListenerEvent event) {
        event.addListener(dataConfig);
    }
    
    @SubscribeEvent
    public void syncCustomData(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ((IDataConfig) dataConfig).syncToClient(event.getPlayer()); // the casts are necessary
        }
        else {
            event.getPlayerList().getPlayers().forEach(((IDataConfig) dataConfig)::syncToClient);
        }
    }
}
