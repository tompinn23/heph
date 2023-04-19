package com.tompinn23.euthenia.lib.config;

import com.tompinn23.euthenia.lib.registry.IVariant;

public interface IEnergyConfig<V extends Enum<V> & IVariant<V>> {
    long capacity(V variant);
    long transfer(V variant);
}
