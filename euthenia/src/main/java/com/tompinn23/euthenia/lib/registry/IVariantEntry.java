package com.tompinn23.euthenia.lib.registry;

public interface IVariantEntry<V extends IVariant, R extends IVariantEntry<V, R>> {
    V getVariant();
}
