package com.tompinn23.euthenia.lib.logistics.energy;

import com.tompinn23.euthenia.lib.logistics.Transfer;
import net.minecraft.core.Direction;

public interface IEnergyStorage {
    Transfer getTransferType();

    boolean isEnergyPresent(Direction side);
}
