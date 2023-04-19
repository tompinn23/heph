package com.tompinn23.euthenia.lib.block;

import com.tompinn23.euthenia.lib.registry.IVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractTickingTile<V extends IVariant, B extends AbstractBlock<V, B>> extends AbstractTile<V, B> {

    private int syncTicks;
    public int ticks;

    public AbstractTickingTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AbstractTickingTile(BlockEntityType<?> type, BlockPos pos, BlockState state, V variant) {
        super(type, pos, state, variant);
    }

    public void tick() {
        final Level world = this.level;
        if (world != null) {
            if (this.ticks == 0) {
                onFirstTick(world);
            }
            if (doPostTicks(world)) {
                int i = postTick(world);
                if (i > -1 && !isRemote()) {
                    sync(i);
                }
            }
            this.ticks++;
            if (!isRemote()) {
                if (this.syncTicks > -1)
                    this.syncTicks--;
                if (this.syncTicks == 0)
                    sync();
            } else {
                clientTick(world);
            }
        }
    }

    protected void onFirstTick(Level world) {
    }

    protected boolean doPostTicks(Level world) {
        return true;
    }

    protected int postTick(Level world) {
        return -1;
    }

    protected void clientTick(Level world) {
    }

    public void sync(int delay) {
        if (!isRemote()) {
            if (this.syncTicks <= 0 || delay < this.syncTicks) {
                this.syncTicks = delay;
            }
        }
    }
}
