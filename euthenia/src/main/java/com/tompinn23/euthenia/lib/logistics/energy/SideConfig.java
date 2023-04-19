package com.tompinn23.euthenia.lib.logistics.energy;

import com.tompinn23.euthenia.lib.logistics.Transfer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SideConfig<T extends IEnergyStorage> {
    private final Transfer[] transfers = new Transfer[6];
    private final T storage;
    private boolean isSetFromNBT;

    public SideConfig(T storage) {
        this.storage = storage;
        Arrays.fill(this.transfers, Transfer.NONE);
    }

    public void init() {
        if (!this.isSetFromNBT) {
            for (Direction side : Direction.values()) {
                setType(side, this.storage.getTransferType());
            }
        }
    }

    public void read(CompoundTag nbt) {
        if (nbt.contains("side_transfer_type", Tag.TAG_INT_ARRAY)) {
            int[] arr = nbt.getIntArray("side_transfer_type");
            for (int i = 0; i < arr.length; i++) {
                this.transfers[i] = Transfer.values()[arr[i]];
            }
            this.isSetFromNBT = true;
        }
    }

    public CompoundTag write(CompoundTag nbt) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0, valuesLength = this.transfers.length; i < valuesLength; i++) {
            list.add(i, this.transfers[i].ordinal());
        }
        nbt.putIntArray("side_transfer_type", list);
        return nbt;
    }

    public void nextTypeAll() {
        if (isAllEquals()) {
            for (Direction side : Direction.values()) {
                nextType(side);
            }
        } else {
            for (Direction side : Direction.values()) {
                setType(side, Transfer.ALL);
            }
        }
    }

    public boolean isAllEquals() {
        boolean flag = true;
        int first = -1;
        for (int i = 1; i < 6; i++) {
            if (this.storage.isEnergyPresent(Direction.from3DDataValue(i))) {
                if (first < 0) {
                    first = this.transfers[i].ordinal();
                } else if (this.transfers[i].ordinal() != first) {
                    flag = false;
                }
            }
        }
        return flag;
    }

    public void nextType(@Nullable Direction side) {
        setType(side, getType(side).next(this.storage.getTransferType()));
    }

    public Transfer getType(@Nullable Direction side) {
        if (side != null) {
            return this.transfers[side.get3DDataValue()];
        }
        return Transfer.NONE;
    }

    public void setType(@Nullable Direction side, Transfer type) {
        if (side == null || this.storage.getTransferType().equals(Transfer.NONE))
            return;
        if (!this.storage.isEnergyPresent(side))
            return;
        this.transfers[side.get3DDataValue()] = type;
    }
}
