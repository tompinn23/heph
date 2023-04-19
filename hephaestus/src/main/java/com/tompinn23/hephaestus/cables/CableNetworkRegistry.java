package com.tompinn23.hephaestus.cables;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;

public class CableNetworkRegistry {

    public static CableNetworkRegistry INSTANCE = new CableNetworkRegistry();

    private HashMap<Level, CableNetworkLevelRegistry> _registries = new HashMap<Level, CableNetworkLevelRegistry>();

    public void onChunkLoaded(LevelAccessor level, ChunkPos pos) {
        if(this._registries.containsKey(level)) {
            this._registries.get(level).onChunkLoaded(pos);
        }
    }

    public void onWorldUnloaded(LevelAccessor level) {
        if(this._registries.containsKey(level)) {
            this._registries.get(level).onWorldUnloaded();
            this._registries.remove(level);
        }
    }

    public void tickStart(LevelAccessor level) {

    }

}
