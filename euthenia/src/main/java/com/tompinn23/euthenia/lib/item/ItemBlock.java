package com.tompinn23.euthenia.lib.item;

import com.tompinn23.euthenia.lib.block.AbstractBlock;
import com.tompinn23.euthenia.lib.block.IBlock;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.registry.IVariantEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ItemBlock<V extends IVariant, B extends Block & IBlock<V, B>> extends BlockItem implements IItem, IVariantEntry<V, B> {
    private final B block;

    @SuppressWarnings("ConstantConditions")
    public ItemBlock(B block, @NotNull Properties builder) {
        super(block, builder);
        this.block = block;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (this.block instanceof AbstractBlock) {
            return ((AbstractBlock) this.block).getDisplayName(stack);
        }
        return super.getName(stack);
    }

    @Override
    public B getBlock() {
        return this.block;
    }


    @Override
    public ItemModelType getItemModelType() {
        return ItemModelType.BLOCK;
    }

    @Override
    public V getVariant() {
        return getBlock().getVariant();
    }
}
