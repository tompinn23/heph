package com.tompinn23.hephaestus.cables;

import com.tompinn23.hephaestus.Hephaestus;
import io.netty.util.collection.ByteCollections;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class CableNetworkLevelRegistry {

    private Level level;

    private final Set<CableNetwork> grids = new HashSet<>();
    private final Set<CableNetwork> dirty = new HashSet<>();
    private final Set<CableNetwork> dead = new HashSet<>();

    private final Set<ICable> detached = new HashSet<>();
    private Set<ICable> orphans = new HashSet<>();
    private final Object orphansMutex = new Object();
    private final HashMap<Long, Set<ICable>> partsAwaitingLoad = new HashMap<>();
    private final Object partsAwaitingLoadMutex = new Object();

    public CableNetworkLevelRegistry(final Level world) {
        this.level = world;
    }

    public void tickStart() {
        if(grids.size() > 0) {
            for(CableNetwork grid: grids) {
                if(grid.level == level && grid.level.isClientSide == level.isClientSide) {
                    if(grid.isEmpty()) {
                        dead.add(grid);
                    } else {
                        grid.update();
                    }
                }
            }
        }
    }


    public void processNetworkChanges() {
        BlockPos pos;
        List<Set<CableNetwork>> mergePools = null;
        if(orphans.size() > 0) {
            Set<ICable> orphans = null;
            synchronized (orphansMutex) {
                if (this.orphans.size() > 0) {
                    orphans = this.orphans;
                    this.orphans = new HashSet<>();
                }
            }
            if(orphans != null && orphans.size() > 0) {
                ChunkSource provider = this.level.getChunkSource();
                Set<CableNetwork> compatiblesNets = new HashSet<>();
                for(ICable cable: orphans) {
                    pos = cable.getPos();
                    if(!this.level.isLoaded(pos)) {
                        continue;
                    }
                    if(cable.isInvalid()) {
                        continue;
                    }
                    if(this.getPartFromLevel(level, pos) != cable) {
                        continue;
                    }
                    compatiblesNets = cable.attachToNeighbours();
                    if(compatiblesNets == null) {
                        CableNetwork net = cable.newNetwork();
                        net.attach(cable);
                        this.grids.add(net);
                    } else if(compatiblesNets.size() > 1) {
                        if(mergePools == null) {
                            mergePools = new ArrayList<>();
                        }
                        boolean hasAddedToPool = false;
                        List<Set<CableNetwork>> candidatePools = new ArrayList<>();
                        for(Set<CableNetwork> pool: mergePools) {
                            if(!Collections.disjoint(candidatePools, compatiblesNets)) {
                                candidatePools.add(pool);
                            }
                        }

                        if(candidatePools.size() <= 0) {
                            mergePools.add(compatiblesNets);
                        } else if(candidatePools.size() == 1) {
                            candidatePools.get(0).addAll(compatiblesNets);
                        } else {
                            Set<CableNetwork> masterPool = candidatePools.get(0);
                            Set<CableNetwork> consumedPool;
                            for(int i = 1; i < candidatePools.size(); i++) {
                                consumedPool = candidatePools.get(i);
                                masterPool.addAll(consumedPool);
                                mergePools.remove(consumedPool);
                            }
                            masterPool.addAll(compatiblesNets);
                        }
                    }
                }
            }

            if(mergePools != null && mergePools.size() > 0) {
                for(Set<CableNetwork> pool: mergePools) {
                    CableNetwork newMaster = null;
                    for(CableNetwork controller: pool) {
                        if(newMaster == null || controller.shouldConsume(newMaster)) {
                            newMaster = controller;
                        }
                    }

                    if(newMaster == null) {
                        Hephaestus.LOGGER.error("Cable network checked a merge pool of %d, found no master candidates", pool.size());
                    } else {
                        addDirtyGrid(newMaster);
                        for(CableNetwork controller: pool) {
                            if(controller != newMaster) {
                                newMaster.assimilate(controller);
                                addDeadGrid(controller);
                                addDirtyGrid(newMaster);
                            }
                        }
                    }
                }
            }
            if(dirty.size() > 0) {
                Set<ICable> newlyDetached = null;
                for(CableNetwork grid: dirty) {
                    newlyDetached = grid.checkForDisconnections();
                    if (grid.isEmpty()) {
                        addDeadGrid(grid);
                    }

                    if(newlyDetached != null && newlyDetached.size() > 0) {
                        detached.addAll(newlyDetached);
                    }
                }
                dirty.clear();
            }
            if(dead.size() > 0) {
                for(CableNetwork grid: dead) {
                    if(!grid.isEmpty()) {
                        Hephaestus.LOGGER.error("Found non empty controller. Forcing it to shed all parts.");
                        detached.addAll(grid.detachAll());
                    }
                    this.grids.remove(grid);
                }
                dead.clear();
            }
            for(ICable cable: detached) {
                cable.assertDetached();
            }

            addAllOrphanedPartsThreadsafe(detached);
            detached.clear();
        }
    }

    private void addOrphanedPartThreadsafe(final ICable part) {
        synchronized(orphansMutex) {
            orphans.add(part);
        }
    }

    private void addAllOrphanedPartsThreadsafe(final Collection<? extends ICable> parts) {
        synchronized(orphansMutex) {
            orphans.addAll(parts);
        }
    }

    public void addDeadGrid(CableNetwork grid) {
        this.dead.add(grid);
    }

    public void addDirtyGrid(CableNetwork grid) {
        this.dirty.add(grid);
    }

    private ICable getPartFromLevel(Level level, BlockPos pos) {
        BlockEntity e = level.getBlockEntity(pos);
        return e instanceof ICable ? (ICable) e : null;
    }

    public void onPartAdded(ICable part) {
        BlockPos worldLocation = part.getPos();
        if(!this.level.isLoaded(worldLocation)) {
            Set<ICable> partSet;
            long chunkHash = new ChunkPos(worldLocation).hashCode();
            synchronized (partsAwaitingLoadMutex) {
                if(!partsAwaitingLoad.containsKey(chunkHash)) {
                    partSet = new HashSet<>();
                    partsAwaitingLoad.put(chunkHash, partSet);
                }
                else {
                    partSet = partsAwaitingLoad.get(chunkHash);
                }

                partSet.add(part);
            }
        } else {
            addOrphanedPartThreadsafe(part);
        }

    }

    public void onConnectorRemoved(ICable part) {
        final BlockPos coord = part.getPos();
        if(coord != null) {
            long hash = new ChunkPos(coord).hashCode();

            if(partsAwaitingLoad.containsKey(hash)) {
                synchronized(partsAwaitingLoadMutex) {
                    if(partsAwaitingLoad.containsKey(hash)) {
                        partsAwaitingLoad.get(hash).remove(part);
                        if(partsAwaitingLoad.get(hash).size() <= 0) {
                            partsAwaitingLoad.remove(hash);
                        }
                    }
                }
            }
        }

        detached.remove(part);
        if(orphans.contains(part)) {
            synchronized(orphansMutex) {
                orphans.remove(part);
            }
        }

        part.assertDetached();
    }


    public void onWorldUnloaded() {
        grids.clear();
        dead.clear();
        dirty.clear();
        detached.clear();
        synchronized (partsAwaitingLoadMutex) {
            partsAwaitingLoad.clear();
        }
        synchronized (orphansMutex) {
            orphans.clear();
        }
        this.level = null;
    }


    public void onChunkLoaded(ChunkPos pos) {
        final long chunkHash = pos.hashCode();
        if(partsAwaitingLoad.containsKey(chunkHash)) {
            synchronized(partsAwaitingLoadMutex) {
                if(partsAwaitingLoad.containsKey(chunkHash)) {
                    addAllOrphanedPartsThreadsafe(partsAwaitingLoad.get(chunkHash));
                    partsAwaitingLoad.remove(chunkHash);
                }
            }
        }
    }
}
