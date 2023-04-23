package com.tompinn23.hephaestus.block;

import com.tompinn23.euthenia.lib.block.AbstractBlock;
import com.tompinn23.euthenia.lib.config.IConfigHolder;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.hephaestus.config.IMachineConfig;

public abstract class AbstractMachineBlock<T extends Enum<T> & IVariant<T>, B extends AbstractBlock<T, B>> extends AbstractBlock<T, B> implements IConfigHolder<T, IMachineConfig<T>> {
    public AbstractMachineBlock(Properties props) {
        this(props, IVariant.EMPTY());
    }

    public AbstractMachineBlock(Properties properties, T variant) {
        super(properties, variant);
    }
}
