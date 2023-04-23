package com.tompinn23.hephaestus.config.type;

import com.tompinn23.euthenia.lib.config.IConfigHolder;
import com.tompinn23.hephaestus.config.IMachineConfig;
import com.tompinn23.hephaestus.config.Tier;
import com.tompinn23.hephaestus.config.values.TieredIntValues;
import com.tompinn23.hephaestus.config.values.TieredLongValues;

public class MachineConfigs implements IMachineConfig<Tier> {

    private TieredIntValues maxVoltage;
    private TieredIntValues maxAmperage;
    private TieredLongValues capacity;

    public MachineConfigs(TieredIntValues maxVoltage, TieredIntValues maxAmperage, TieredLongValues capacity) {
        this.maxVoltage = maxVoltage;
        this.maxAmperage = maxAmperage;
        this.capacity = capacity;
    }

    @Override
    public long capacity(Tier variant) {
        return this.capacity.get(variant);
    }

    @Override
    public int maxVoltage(Tier variant) {
        return this.maxVoltage.get(variant);
    }

    @Override
    public int maxAmperage(Tier variant) {
        return this.maxAmperage.get(variant);
    }
}
