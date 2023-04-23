package com.tompinn23.hephaestus.cables;

import com.tompinn23.hephaestus.Hephaestus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;

import java.nio.file.Path;
import java.util.*;

public abstract class CableNetwork {

    public final Level level;
    private BlockPos refPos = null;
    private boolean shouldCheckForDisconnect = true;
    protected HashSet<ICable> connectedParts = new HashSet<>();

    protected Set<ICable> generatingCables = new HashSet<>();

    protected HashMap<Tuple<BlockPos, BlockPos>, CachedPath> cachedPaths = new HashMap<>();

    protected CableNetwork(Level level) {
        this.level = level;
    }

    /*
     * Called when the a tile entity is changed neighbouring the network.
     * see @link IForgeBlock#onNeighborChange
     */
    public abstract void onNeighbourBlockChanged(BlockPos pos, Direction direction);

    /**
     * Call when a block with cached save-delegate data is added to the multiblock.
     * The part will be notified that the data has been used after this call completes.
     * @param part The NBT tag containing this controller's data.
     */
    public abstract void onAttachedPartWithNetData(ICable part, CompoundTag data);

    /**
     * Check if a block is being tracked by this machine.
     * @param blockCoord Coordinate to check.
     * @return True if the tile entity at blockCoord is being tracked by this machine, false otherwise.
     */
    public boolean hasBlock(BlockPos blockCoord) {
        return connectedParts.contains(blockCoord);
    }

    /**
     * Attach a new part to this machine.
     * @param part The part to add.
     */
    public void attach(ICable part) {
        ICable candidate;
        BlockPos coord = part.getPos();

        if(!connectedParts.add(part))
            Hephaestus.LOGGER.warn("[%s] CableGrid %s is double-adding part %d @ %s. This is unusual. If you encounter odd behavior, please tear down the machine and rebuild it.",
                    (level.isClientSide ? "CLIENT" : "SERVER"), hashCode(), part.hashCode(), coord);

        part.onAttached(this);
        this.onBlockAdded(part);

        if(part.hasNetworkNBT()) {
            CompoundTag savedData = part.getNetworkNBTData();
            onAttachedPartWithNetData(part, savedData);
            part.onNetworkNBTAssimilated();
        }

        if(this.refPos == null) {
            refPos = coord;
            part.becomeNBTDelegate();
        }
        else if(coord.compareTo(refPos) < 0) {
            BlockEntity te = this.level.getBlockEntity(refPos);
            if(te != null) {
                ((ICable)te).forfeitNBTDelegate();
            } else {
                Hephaestus.LOGGER.warn("Attempted to forfeit NBT on null te");
            }

            refPos = coord;
            part.becomeNBTDelegate();
        }
        else {
            part.forfeitNBTDelegate();
        }


        REGISTRY.addDirtyGrid(level, this);
    }
    /**
     * Called when a new part is added to the machine. Good time to register things into lists.
     * @param newPart The part being added.
     */
    protected abstract void onBlockAdded(ICable newPart);

    /**
     * Called when a part is removed from the machine. Good time to clean up lists.
     * @param oldPart The part being removed.
     */
    protected abstract void onBlockRemoved(ICable oldPart);

    public boolean isEmpty() {
        return connectedParts.isEmpty();
    }

    /**
     * Callback whenever a part is removed (or will very shortly be removed) from a controller.
     * Do housekeeping/callbacks, also nulls min/max coords.
     * @param part The part being removed.
     */
    private void onDetachBlock(ICable part) {
        // Strip out this part
        part.onDetached(this);
        this.onBlockRemoved(part);
        part.forfeitNBTDelegate();

        if(refPos != null && refPos.equals(part.getPos())) {
            refPos = null;
        }

        shouldCheckForDisconnect = true;
    }

    /**
     * Call to detach a block from this machine. Generally, this should be called
     * when the tile entity is being released, e.g. on block destruction.
     * @param part The part to detach from this machine.
     * @param chunkUnloading Is this entity detaching due to the chunk unloading? If true, the multiblock will be paused instead of broken.
     */
    public void detach(ICable part, boolean chunkUnloading) {
//        if(chunkUnloading && this.assemblyState == MultiblockControllerBase.AssemblyState.Assembled) {
//            this.assemblyState = MultiblockControllerBase.AssemblyState.Paused;
//            this.onMachinePaused();
//        }

        // Strip out this part
        onDetachBlock(part);
        if(!connectedParts.remove(part)) {
            BlockPos position = part.getPos();

            Hephaestus.LOGGER.warn("[%s] Double-removing part (%d) @ %d, %d, %d, this is unexpected and may cause problems. If you encounter anomalies, please tear down the reactor and rebuild it.",
                    this.level.isClientSide ? "CLIENT" : "SERVER", part.hashCode(), position.getX(), position.getY(), position.getZ());
        }

        if(connectedParts.isEmpty()) {
            // Destroy/unregister
            REGISTRY.addDeadGrid(this.level, this);
            return;
        }

        REGISTRY.addDirtyGrid(this.level, this);

        // Find new save delegate if we need to.
        if(refPos == null) {
            selectNewRefPosition();
        }
    }
    /**
     * Assimilate another controller into this controller.
     * Acquire all of the other controller's blocks and attach them
     * to this one.
     *
     * @param other The controller to merge into this one.
     */
    public void assimilate(CableNetwork other) {
        BlockPos otherRefPos = other.getRefPos();
        if(otherRefPos != null && getRefPos().compareTo(otherRefPos) >= 0) {
            throw new IllegalArgumentException("The controller with the lowest minimum-coord value must consume the one with the higher coords");
        }

        BlockEntity te;
        Set<ICable> partsToAcquire = new HashSet<>(other.connectedParts);

        // releases all blocks and references gently so they can be incorporated into another multiblock
        other._onAssimilated(this);

        for(ICable acquiredPart : partsToAcquire) {
            // By definition, none of these can be the minimum block.
            if(acquiredPart.isInvalid()) { continue; }

            connectedParts.add(acquiredPart);
            acquiredPart.onAssimilated(this);
            this.onBlockAdded(acquiredPart);
        }

        this.onAssimilate(other);
        other.onAssimilated(this);
    }

    protected BlockPos getRefPos() {
        if(refPos == null) { selectNewRefPosition(); }
        return refPos;
    }

    /**
     * Called when this machine is consumed by another controller.
     * Essentially, forcibly tear down this object.
     * @param otherController The controller consuming this controller.
     */
    private void _onAssimilated(CableNetwork otherController) {
        if(refPos != null) {
            if (this.level.isLoaded(this.refPos)) {
                BlockEntity te = this.level.getBlockEntity(this.refPos);
                if(te instanceof ICable<?>) {
                    ((ICable)te).forfeitNBTDelegate();
                }
            }
            this.refPos = null;
        }

        connectedParts.clear();
    }

    /**
     * Callback. Called after this controller assimilates all the blocks
     * from another controller.
     * Use this to absorb that controller's game data.
     * @param assimilated The controller whose uniqueness was added to our own.
     */
    protected abstract void onAssimilate(CableNetwork assimilated);

    /**
     * Callback. Called after this controller is assimilated into another controller.
     * All blocks have been stripped out of this object and handed over to the
     * other controller.
     * This is intended primarily for cleanup.
     * @param assimilator The controller which has assimilated this controller.
     */
    protected abstract void onAssimilated(CableNetwork assimilator);


    public final void update() {
        if(connectedParts.isEmpty()) {
            REGISTRY.addDeadGrid(this.level, this);
            return;
        }

        if(level.isClientSide)
            updateClient();
        else if(updateServer()) {
            ChunkAccess chunkToSave = this.level.getChunk(refPos);
            chunkToSave.setUnsaved(true);
        }


    }

    protected abstract boolean updateServer();

    protected abstract void updateClient();

    public abstract CompoundTag write(CompoundTag data);
    public abstract void read(CompoundTag data);

    /**
     * Called when the save delegate's tile entity is being asked for its description packet
     * @param data A fresh compound tag to write your multiblock data into
     */
    public abstract void formatDescriptionPacket(CompoundTag data);

    /**
     * Called when the save delegate's tile entity receiving a description packet
     * @param data A compound tag containing multiblock data to import
     */
    public abstract void decodeDescriptionPacket(CompoundTag data);

    /**
     * Tests whether this multiblock should consume the other multiblock
     * and become the new multiblock master when the two multiblocks
     * are adjacent. Assumes both multiblocks are the same type.
     * @param otherController The other multiblock controller.
     * @return True if this multiblock should consume the other, false otherwise.
     */
    public boolean shouldConsume(CableNetwork otherController) {
        if(!otherController.getClass().equals(getClass())) {
            throw new IllegalArgumentException("Attempting to merge two energy grid with different master classes - this should never happen!");
        }

        if(otherController == this) { return false; } // Don't be silly, don't eat yourself.

        int res = _shouldConsume(otherController);
        if(res < 0) { return true; }
        else if(res > 0) { return false; }
        else {
            // Strip dead parts from both and retry
            Hephaestus.LOGGER.warn("[%s] Encountered two grids with the same reference coordinate. Auditing connected parts and retrying.", this.level.isClientSide ? "CLIENT" : "SERVER");

            auditParts();
            otherController.auditParts();

            res = _shouldConsume(otherController);
            if(res < 0) { return true; }
            else if(res > 0) { return false; }
            else {
                Hephaestus.LOGGER.error("My Controller (%d): size (%d), parts: %s", hashCode(), this.connectedParts.size(), this.getPartsListString());
                Hephaestus.LOGGER.error("Other Controller (%d): size (%d), coords: %s", otherController.hashCode(), otherController.connectedParts.size(), otherController.getPartsListString());
                throw new IllegalArgumentException("[" + (this.level.isClientSide ? "CLIENT" : "SERVER") + "] Two controllers with the same reference coord that somehow both have valid parts - this should never happen!");
            }

        }
    }


    /**
     * Checks all of the parts in the controller. If any are dead or do not exist in the world, they are removed.
     */
    private void auditParts() {
        HashSet<ICable> deadParts = new HashSet<ICable>();
        for(ICable part : connectedParts) {
            if(part.isInvalid() || level.getBlockEntity(part.getPos()) != part) {
                onDetachBlock(part);
                deadParts.add(part);
            }
        }

        connectedParts.removeAll(deadParts);
        Hephaestus.LOGGER.warn("[%s] Grid found %d dead parts during an audit, %d parts remain attached", this.level.isClientSide ? "CLIENT" : "SERVER", deadParts.size(), this.connectedParts.size());
    }

    private String getPartsListString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        BlockPos partPos;
        for(ICable part : connectedParts) {
            if(!first) {
                sb.append(", ");
            }
            partPos = part.getPos();
            sb.append(String.format("(%d: %d, %d, %d)", part.hashCode(), partPos.getX(), partPos.getY(), partPos.getZ()));
            first = false;
        }

        return sb.toString();
    }

    private int _shouldConsume(CableNetwork otherController) {
        BlockPos myCoord = getRefPos();
        BlockPos theirCoord = otherController.getRefPos();

        // Always consume other controllers if their reference coordinate is null - this means they're empty and can be assimilated on the cheap
        if(theirCoord == null) { return -1; }
        else { return myCoord.compareTo(theirCoord); }
    }

    private static final CableNetworkRegistry REGISTRY;

    static {
        REGISTRY = CableNetworkRegistry.INSTANCE;
    }

    /**
     * Called when this machine may need to check for blocks that are no
     * longer physically connected to the reference coordinate.
     * @return
     */
    public Set<ICable> checkForDisconnections() {
        if(!this.shouldCheckForDisconnect) {
            return null;
        }

        if(this.isEmpty()) {
            REGISTRY.addDeadGrid(level, this);
            return null;
        }

        BlockEntity te;
        ChunkSource chunkProvider = level.getChunkSource();

        // Invalidate our reference coord, we'll recalculate it shortly
        refPos = null;

        // Reset visitations and find the minimum coordinate
        Set<ICable> deadParts = new HashSet<ICable>();
        BlockPos position;
        ICable referencePart = null;

        int originalSize = connectedParts.size();

        for(ICable part : connectedParts) {
            position = part.getPos();
            // This happens during chunk unload.
            if (!this.level.isLoaded(position) || part.isInvalid()) {
                deadParts.add(part);
                onDetachBlock(part);
                continue;
            }

            if(level.getBlockEntity(position) != part) {
                deadParts.add(part);
                onDetachBlock(part);
                continue;
            }

            part.setUnvisited();
            part.forfeitNBTDelegate();

            if(refPos == null) {
                refPos = position;
                referencePart = part;
            }
            else if(position.compareTo(refPos) < 0) {
                refPos = position;
                referencePart = part;
            }
        }

        connectedParts.removeAll(deadParts);
        deadParts.clear();

        if(referencePart == null || isEmpty()) {
            // There are no valid parts remaining. The entire multiblock was unloaded during a chunk unload. Halt.
            shouldCheckForDisconnect = false;
            REGISTRY.addDeadGrid(level, this);
            return null;
        }
        else {
            referencePart.becomeNBTDelegate();
        }

        // Now visit all connected parts, breadth-first, starting from reference coord's part
        ICable part;
        LinkedList<ICable> partsToCheck = new LinkedList<ICable>();
        ICable[] nearbyParts = null;
        int visitedParts = 0;

        partsToCheck.add(referencePart);

        while(!partsToCheck.isEmpty()) {
            part = partsToCheck.removeFirst();
            part.setVisited();
            visitedParts++;

            nearbyParts = part.getNeighbouringParts(); // Chunk-safe on server, but not on client
            for(ICable nearbyPart : nearbyParts) {
                // Ignore different machines
                if(nearbyPart.getNetwork() != this) {
                    continue;
                }

                if(!nearbyPart.isVisited()) {
                    nearbyPart.setVisited();
                    partsToCheck.add(nearbyPart);
                }
            }
        }

        // Finally, remove all parts that remain disconnected.
        Set<ICable> removedParts = new HashSet<ICable>();
        for(ICable orphanCandidate : connectedParts) {
            if (!orphanCandidate.isVisited()) {
                deadParts.add(orphanCandidate);
                orphanCandidate.onOrphaned(this, originalSize, visitedParts);
                onDetachBlock(orphanCandidate);
                removedParts.add(orphanCandidate);
            }
        }

        // Trim any blocks that were invalid, or were removed.
        connectedParts.removeAll(deadParts);

        // Cleanup. Not necessary, really.
        deadParts.clear();

        // Juuuust in case.
        if(refPos == null) {
            selectNewRefPosition();
        }

        // We've run the checks from here on out.
        shouldCheckForDisconnect = false;

        return removedParts;
    }

    /**
     * Detach all parts. Return a set of all parts which still
     * have a valid tile entity. Chunk-safe.
     * @return A set of all parts which still have a valid tile entity.
     */
    public Set<ICable> detachAll() {
        if(level == null) { return new HashSet<ICable>(); }

        ChunkSource chunkProvider = level.getChunkSource();
        for(ICable part : connectedParts) {
            if(this.level.isLoaded(part.getPos())) {
                onDetachBlock(part);
            }
        }

        Set<ICable> detachedParts = connectedParts;
        connectedParts = new HashSet<ICable>();
        return detachedParts;
    }

    private void selectNewRefPosition() {
        ChunkSource chunkProvider = level.getChunkSource();
        ICable theChosenOne = null;
        BlockPos position;

        refPos = null;

        for(ICable part : connectedParts) {
            position = part.getPos();
            if(part.isInvalid() || !this.level.isLoaded(position)) {
                // Chunk is unloading, skip this coord to prevent chunk thrashing
                continue;
            }

            if(refPos == null || refPos.compareTo(position) > 0) {
                refPos = position;
                theChosenOne = part;
            }
        }

        if(theChosenOne != null) {
            theChosenOne.becomeNBTDelegate();
        }
    }

    /**
     * Marks the reference coord dirty & updateable.
     *
     * On the server, this will mark the for a data-update, so that
     * nearby clients will receive an updated description packet from the server
     * after a short time. The block's chunk will also be marked dirty and the
     * block's chunk will be saved to disk the next time chunks are saved.
     *
     * On the client, this will mark the block for a rendering update.
     */
    protected void markrefPosForUpdate() {

        BlockPos rc = this.getRefPos();

        if ((this.level != null) && (rc != null))
            level.setBlockAndUpdate(rc, level.getBlockState(rc));
    }

    /**
     * Marks the reference coord dirty.
     *
     * On the server, this marks the reference coord's chunk as dirty; the block (and chunk)
     * will be saved to disk the next time chunks are saved. This does NOT mark it dirty for
     * a description-packet update.
     *
     * On the client, does nothing.
     * @see AbstractCableNetwork#markrefPosForUpdate()
     */
    protected void markrefPosDirty() {
        if(level == null || level.isClientSide) { return; }

        BlockPos refPos = this.getRefPos();
        if(refPos == null) { return; }

        BlockEntity saveTe = level.getBlockEntity(refPos);
        level.setBlocksDirty(refPos, saveTe.getBlockState(), saveTe.getBlockState());
    }

    /**
     * Marks the whole multiblock for a render update on the client. On the server, this does nothing
     */
    protected void markMultiblockForRenderUpdate() {
//        for(int x = getMinimumCoord().getX(); x <= this.getMaximumCoord().getX(); x++)
//            for(int y = getMinimumCoord().getY(); y <= this.getMaximumCoord().getY(); y++)
//                for(int z = getMinimumCoord().getZ(); z <= this.getMaximumCoord().getZ(); z++) {
//                    BlockPos p = new BlockPos(x, y, z);
//                    this.level.markBlockRangeForRenderUpdate(p, level.getBlockState(p), level.getBlockState(p));
//                }
    }

    public void splitNode(ICable originator) {
//        List<Set<ICable>> sets = new ArrayList<>();
//        //TODO: Split node blocks
//        for(var neighbour : originator.getNeighbouringParts()) {
//            Set<ICable> pool = new HashSet<>();
//            pool.add(neighbour);
//            pool.addAll(Arrays.stream(neighbour.getNeighbouringParts()).filter(part -> part != this).toList());
//            sets.add(pool);
//        }
//        for(ICable part : nodeMap.get(originator.getNodeSetPos())) {
//            part.setUnvisited();
//        }
//        List<Set<ICable>> newSets = new ArrayList<>();
//        for(var set : sets) {
//            Set<ICable> visited = new HashSet<>();
//            Set<ICable> unvisited = new HashSet<>();
//            unvisited.addAll(set);
//            for(ICable part : unvisited) {
//                visited.add(part);
//                unvisited.addAll(Arrays.stream(part.getNeighbouringParts()).filter(p -> !p.isVisited()).toList());
//                part.setVisited();
//            }
//            BlockPos ref = visited.stream().map(c -> c.getPos()).min(BlockPos::compareTo).get();
//            visited.stream().forEach(c -> c.setNodeSetPos(ref));
//            nodeMap.put(ref, visited);
//        }
    }

    private class PathFinder {

        private class PathNode {
            public ICable part;
            public PathNode parent;

            public double g;
            public double f;


            public boolean closed;
            public boolean open;

            public PathNode(ICable part, PathNode parent) {
                this.part = part;
                this.parent = parent;
                this.g = 0.0;
                this.f = 0.0;
            }

            public double getF() {
                return f;
            }

            public PathNode[] getNeighbouringParts() {
                return Arrays.stream(part.getNeighbouringParts()).map(part -> { return new PathNode(part, this); }).toArray(PathNode[]::new);
            }
        }

        private PriorityQueue<PathNode> openList;
        private ICable start;
        private ICable end;

        public PathFinder(ICable start, ICable end) {
            this.start = start;
            this.end = end;
            openList = new PriorityQueue<>(Comparator.comparingDouble(PathNode::getF));
        }

        public List<ICable> findPath() {;
            var start = new PathNode(this.start, null);
            start.open = true;
            openList.add(start);
            while(!openList.isEmpty()) {
                var node = openList.poll();
                node.closed = true;
                if(node.part == end) {
                    List<ICable > path = new ArrayList<>();
                    while(node.parent != null) {
                        path.add(node.part);
                        node = node.parent;
                    }
                    Collections.reverse(path);
                    return path;
                }
                for(var child : node.getNeighbouringParts()) {
                    if(child.closed) {
                        continue;
                    }
                    var ng = node.g + distance(child, node);
                    child.f = child.g + distance(child, new PathNode(end, null));
                    if(child.open) {
                        if(ng > child.g) {
                            continue;
                        }
                    }
                    child.open = true;
                    openList.add(child);
                }
            }
            return null;
        }


        private double distance(PathNode a, PathNode b) {
            BlockPos aPos = a.part.getPos();
            BlockPos bPos = b.part.getPos();
            double dx = Math.pow(aPos.getX() - bPos.getX(), 2);
            double dy = Math.pow(aPos.getY() - bPos.getY(), 2);
            double dz = Math.pow(aPos.getZ() - bPos.getZ(), 2);
            return Math.sqrt(dx + dy + dz);
        }
    }

    private class CachedPath {
        private ICable start;
        private ICable end;

        private List<ICable> path;

        private boolean dirty;

        public CachedPath(ICable start, ICable end) {
            this.start = start;
            this.end = end;
        }

        public List<ICable> getPath() {
            if(path == null || dirty) {
                path = new PathFinder(start, end).findPath();
            } else {
                return path;
            }
            return null;
        }
    }
}
