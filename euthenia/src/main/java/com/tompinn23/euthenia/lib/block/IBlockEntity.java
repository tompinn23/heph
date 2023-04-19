package com.tompinn23.euthenia.lib.block;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IBlockEntity {
    default void onPlaced(Level world, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    }

    default void onAdded(Level world, BlockState state, BlockState oldState, boolean isMoving) {
    }

    default void onRemoved(Level world, BlockState state, BlockState newState, boolean isMoving) {
    }
}
