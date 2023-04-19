package com.tompinn23.euthenia.lib.item;

import com.tompinn23.euthenia.lib.block.AbstractEnergyBlock;
import com.tompinn23.euthenia.lib.block.IBlock;
import com.tompinn23.euthenia.lib.config.IEnergyConfig;
import com.tompinn23.euthenia.lib.logistics.Transfer;
import com.tompinn23.euthenia.lib.registry.IVariant;
import net.minecraft.world.item.ItemStack;

public class EnergyBlockItem<T extends Enum<T> & IVariant<T>, C extends IEnergyConfig<T>, B extends AbstractEnergyBlock<T, C, B> & IBlock<T, B>> extends ItemBlock<T, B> implements IEnergyItemProvider, IEnergyContainingItem {
    public EnergyBlockItem(B block, Properties builder) {
        super(block, builder);
    }

    @Override
    public Info getEnergyInfo() {
        long transfer = getConfig().transfer(getVariant());
        return new Info(getConfig().capacity(getVariant()), getTransferType().canReceive ? transfer : 0, getTransferType().canExtract ? transfer : 0);
    }

    public Transfer getTransferType() {
        return getBlock().getTransferType();
    }

    public C getConfig() {
        return getBlock().getConfig();
    }

    public T getVariant() {
        return getBlock().getVariant();
    }

    @Override
    public boolean isChargeable(ItemStack stack) {
        return getBlock().isChargeable(stack);
    }
}
