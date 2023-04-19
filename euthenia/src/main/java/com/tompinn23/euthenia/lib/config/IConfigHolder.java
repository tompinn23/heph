package com.tompinn23.euthenia.lib.config;

import com.tompinn23.euthenia.lib.registry.IVariant;

public interface IConfigHolder<V extends Enum<V> & IVariant<V>, C> {
    C getConfig();
}

