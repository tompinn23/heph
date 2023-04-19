package com.tompinn23.hephaestus.cables;

import com.tompinn23.hephaestus.Hephaestus;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hephaestus.MODID)
public class CableEventHandler {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onChunkLoad(final ChunkEvent.Load loadEvent) {
        var chunk = loadEvent.getChunk();
        CableNetworkRegistry.INSTANCE.onChunkLoaded(loadEvent.getLevel(), chunk.getPos());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onWorldUnload(final LevelEvent.Unload unloadEvent) {
        CableNetworkRegistry.INSTANCE.onWorldUnloaded(unloadEvent.getLevel());
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent event) {
        if(TickEvent.Phase.START == event.phase) {
            CableNetworkRegistry.INSTANCE.tickStart(event.level);
        }
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if(TickEvent.Phase.START == event.phase) {
            CableNetworkRegistry.INSTANCE.tickStart(Minecraft.getInstance().level);
        }
    }
}
