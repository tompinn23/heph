package com.tompinn23.euthenia.lib.block;

import com.tompinn23.euthenia.lib.registry.IVariant;

public class SimpleBlock extends AbstractBlock<IVariant.Single, SimpleBlock> {
    public SimpleBlock(Properties props) {
        super(props, IVariant.EMPTY());
    }
}
