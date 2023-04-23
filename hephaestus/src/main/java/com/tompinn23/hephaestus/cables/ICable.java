package com.tompinn23.hephaestus.cables;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

import java.util.Set;

public interface ICable<T extends CableNetwork> extends IForgeBlockEntity {

    BlockPos getPos();
    void setNodeSetPos(BlockPos pos);
    BlockPos getNodeSetPos();

    boolean isInvalid();

    boolean inNetwork();

    Set<T> attachToNeighbours();

    T newNetwork();

    void assertDetached();

    void onAttached(T network);

    boolean hasNetworkNBT();

    CompoundTag getNetworkNBTData();

    void onNetworkNBTAssimilated();

    void becomeNBTDelegate();

    void forfeitNBTDelegate();

    void onDetached(T cableNetwork);

    void setUnvisited();

    void setVisited();

    ICable[] getNeighbouringParts();
    T getNetwork();

    Class<T> getNetworkType();

    boolean isVisited();

    void onOrphaned(T cableNetwork, int originalSize, int visitedParts);

    void onAssimilated(T cableNetwork);
}
