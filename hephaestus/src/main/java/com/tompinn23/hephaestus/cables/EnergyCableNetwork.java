package com.tompinn23.hephaestus.cables;

import com.tompinn23.hephaestus.Hephaestus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class EnergyCableNetwork extends CableNetwork {
    public EnergyCableNetwork(Level level) {
        super(level);
    }

    @Override
    public void onNeighbourBlockChanged(BlockPos pos, Direction direction) {
        //Hephaestus.LOGGER.info("neighbour block changed: {} {}", pos, direction);
    }

    @Override
    public void onAttachedPartWithNetData(ICable part, CompoundTag data) {

    }

    @Override
    protected void onBlockAdded(ICable newPart) {

    }

    @Override
    protected void onBlockRemoved(ICable oldPart) {

    }

    @Override
    protected void onAssimilate(CableNetwork assimilated) {

    }

    @Override
    protected void onAssimilated(CableNetwork assimilator) {

    }

    @Override
    protected boolean updateServer() {
        return false;
    }

    @Override
    protected void updateClient() {

    }

    @Override
    public CompoundTag write(CompoundTag data) {
        return null;
    }

    @Override
    public void read(CompoundTag data) {

    }

    @Override
    public void formatDescriptionPacket(CompoundTag data) {

    }

    @Override
    public void decodeDescriptionPacket(CompoundTag data) {

    }
}
