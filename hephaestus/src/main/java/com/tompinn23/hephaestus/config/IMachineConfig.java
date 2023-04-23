package com.tompinn23.hephaestus.config;

import com.tompinn23.euthenia.lib.registry.IVariant;

public interface IMachineConfig<V extends Enum<V> & IVariant<V>> {

    long capacity(V variant);
    int maxVoltage(V variant);
    int maxAmperage(V variant);
}
