package com.tompinn23.hephaestus.tile;

import com.tompinn23.euthenia.lib.block.AbstractTile;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.hephaestus.block.CableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 *
 *
 */
public class CableTile extends AbstractTile<IVariant.Single, CableBlock> {

    private EnumSet<Direction> connectedSides = EnumSet.noneOf(Direction.class);
    public CableTile(BlockPos pos, BlockState state) {
        this(pos, state, IVariant.EMPTY());
    }

    public CableTile(BlockPos pos, BlockState state, IVariant.Single variant) {
        super(Tiles.CABLE.get(), pos, state, variant);
    }

    @Override
    public void onPlaced(Level world, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, state, placer, stack);
        for(var side : Direction.values()) {
            var offsetPos = getBlockPos().relative(side);
            var tile = world.getBlockEntity(offsetPos);
            if(tile instanceof CableTile)
                ((CableTile) tile).joinCableNet(this, side.getOpposite());
        }
    }


    public void joinCableNet(CableTile tile, Direction side) {
        joinCableNet();
    }

}
