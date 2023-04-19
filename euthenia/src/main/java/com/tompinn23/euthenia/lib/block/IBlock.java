package com.tompinn23.euthenia.lib.block;

import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.registry.IVariantEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IBlock<V extends IVariant, B extends Block & IBlock<V, B>> extends IVariantEntry<V, B>, EntityBlock {

    @SuppressWarnings("unchecked")
    default BlockItem getBlockItem(Item.Properties props) {
        return new BlockItem((Block) this, props);
    }

    @Nullable
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> be) {
        if(newBlockEntity(BlockPos.ZERO, state) instanceof AbstractTickingTile<?,?>) {
            return (l,p,s, b) -> ((AbstractTickingTile<?,?>) b).tick();
        }
        return null;
    }

}
