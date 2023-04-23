package com.tompinn23.hephaestus.tile;

import com.tompinn23.euthenia.lib.block.AbstractTile;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.util.Util;
import com.tompinn23.hephaestus.Hephaestus;
import com.tompinn23.hephaestus.block.CableBlock;
import com.tompinn23.hephaestus.cables.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 *
 *
 *
 */
public abstract class CableTile<T extends CableNetwork> extends AbstractTile<IVariant.Single, CableBlock> implements ICable<T> {

    private EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
    protected T grid;
    private CompoundTag cachedNBT;
    private BlockPos nodeSetPos;
    private boolean saveData;
    private boolean visited;

    public CableTile(BlockPos pos, BlockState state) {
        this(pos, state, IVariant.EMPTY());
    }

    public CableTile(BlockPos pos, BlockState state, IVariant.Single variant) {
        super(Tiles.CABLE.get(), pos, state, variant);
    }

    @Override
    public void setNodeSetPos(BlockPos pos) {
        this.nodeSetPos = pos;
    }

    @Override
    public BlockPos getNodeSetPos() {
        return this.nodeSetPos;
    }

    public void onNeighbourChange(Direction direction) {
//        var oldSize = connections.size();
//        Util.getBlockEntityAt(CableTile.class, level, getPos().relative(direction)).ifPresentOrElse(tile -> {
//            connections.add(direction);
//        }, () -> {
//            connections.remove(direction);
//        });
//        if(connections.size() <= 2 &&  oldSize > 2) {
//            //TODO: Merge node blocks
//            this.getNetwork().mergeNodes(this);
//        } else if(connections.size() > 2 && oldSize <= 2) {
//            this.getNetwork().splitNode(this);
//        }
    }

    @Override
    public void onPlaced(Level world, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, state, placer, stack);
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public boolean isInvalid() {
        return this.remove;
    }

    @Override
    public boolean inNetwork() {
        return this.grid != null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        CableNetworkRegistry.INSTANCE.onNewCable(this.level, this);
    }

    @Override
    public Set<T> attachToNeighbours() {
        Set<T> controllers = null;
        T best = null;
        ICable<T>[] parts = getNeighbouringParts();
        for (var neighbour : parts) {
            if (neighbour.inNetwork()) {
                var candidate = neighbour.getNetwork();
                if(!(candidate.getClass() == this.getNetworkType())) {
                    continue;
                }
                if(controllers == null) {
                    controllers = new HashSet<>();
                    best = candidate;
                } else if(!controllers.contains(candidate) && candidate.shouldConsume(best)) {
                    best = candidate;
                }
                controllers.add(candidate);
            }
        }
        if(best != null) {
            this.grid = best;
            best.attach(this);
        }
        return controllers;
    }

    @Override
    public void assertDetached() {
        if(this.grid != null) {
            BlockPos pos = getPos();
            Hephaestus.LOGGER.error("[assert] Part (%d, %d, %d) should be detached but it is not. This is not fatal but unusual", pos.getX(), pos.getY(), pos.getZ());
            this.grid = null;
        }
    }

    @Override
    public void onAttached(T network) {
        this.grid = network;
    }

    @Override
    public boolean hasNetworkNBT() {
        return this.cachedNBT != null;
    }

    @Override
    public CompoundTag getNetworkNBTData() {
        return this.cachedNBT;
    }

    @Override
    public void onNetworkNBTAssimilated() {
        this.cachedNBT = null;
    }

    @Override
    public void becomeNBTDelegate() {
        this.saveData = true;
    }

    @Override
    public void forfeitNBTDelegate() {
        this.saveData = false;
    }

    @Override
    public void onDetached(T cableNetwork) {
        this.grid = null;
    }

    @Override
    public void setUnvisited() {
        this.visited = false;
    }

    @Override
    public void setVisited() {
        this.visited = true;
    }

    @Override
    public ICable[] getNeighbouringParts() {
        BlockEntity be;
        List<ICable<T>> neighbours = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            be = getLevel().getBlockEntity(getPos().relative(dir));
            if(be instanceof ICable) {
                neighbours.add((ICable<T>) be);
            }
        }
        return neighbours.toArray(new ICable[0]);
    }

    @Override
    public T getNetwork() {
        return this.grid;
    }

    @Override
    public boolean isVisited() {
        return this.visited;
    }

    @Override
    public void onOrphaned(T cableNetwork, int originalSize, int visitedParts) {
        this.setChangedFast();
    }

    @Override
    public void onAssimilated(T cableNetwork) {
        assert(this.grid != cableNetwork);
        this.grid = cableNetwork;
    }
}
