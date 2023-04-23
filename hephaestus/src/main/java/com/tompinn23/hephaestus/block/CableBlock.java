package com.tompinn23.hephaestus.block;

import com.tompinn23.euthenia.lib.block.AbstractBlock;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.util.Util;
import com.tompinn23.hephaestus.tile.CableTile;
import com.tompinn23.hephaestus.tile.EnergyCableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CableBlock extends AbstractBlock<IVariant.Single, CableBlock> {

    /* DUNSWE */
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;

    public CableBlock(Properties props) {
        this(props, IVariant.EMPTY());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableTile(pos, state);
    }

    public CableBlock(Properties properties, IVariant.Single variant) {
        super(properties, variant);
        setStateProps(state -> state
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(DOWN, false)
                .setValue(UP, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        var world = context.getLevel();
        var pos = context.getClickedPos();
        boolean[] north = canAttach(state, world, pos, Direction.NORTH);
        boolean[] south = canAttach(state, world, pos, Direction.SOUTH);
        boolean[] west = canAttach(state, world, pos, Direction.WEST);
        boolean[] east = canAttach(state, world, pos, Direction.EAST);
        boolean[] up = canAttach(state, world, pos, Direction.UP);
        boolean[] down = canAttach(state, world, pos, Direction.DOWN);
        FluidState fluidState = world.getFluidState(pos);
        return state.setValue(NORTH, north[0] && !north[1]).setValue(SOUTH, south[0] && !south[1]).setValue(WEST, west[0] && !west[1]).setValue(EAST, east[0] && !east[1]).setValue(UP, up[0] && !up[1]).setValue(DOWN, down[0] && !down[1]);
    }

    /**
     *
     * @param state
     * @param level The level
     * @param pos Block position in level
     * @param neighbor Block position of neighbor
     */
    @Override
    public void onNeighborChange(BlockState state, LevelReader level, @NotNull BlockPos pos, BlockPos neighbor) {

    }



    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        Direction facing = Direction.fromNormal(pos.subtract(fromPos)).getOpposite();
        boolean val = false;
        if(level.getBlockEntity(fromPos) instanceof CableTile) {
            val = true;
        }
        switch(facing) {
            case DOWN -> state = state.setValue(DOWN, val);
            case UP -> state = state.setValue(UP, val);
            case NORTH -> state = state.setValue(NORTH, val);
            case SOUTH -> state = state.setValue(SOUTH, val);
            case WEST -> state = state.setValue(WEST, val);
            case EAST -> state = state.setValue(EAST, val);
        }
        level.setBlockAndUpdate(pos, state);
        Util.getBlockEntityAt(EnergyCableTile.class, level, pos).ifPresent(tile -> {
            if(tile.inNetwork()) {
                tile.getNetwork().onNeighbourBlockChanged(pos, facing);
            }
        });
    }

    public boolean[] canAttach(BlockState state, Level world, BlockPos pos, Direction direction) {
        return new boolean[]{world.getBlockState(pos.relative(direction)).getBlock() == this || canConnectEnergy(world, pos, direction), canConnectEnergy(world, pos, direction)};
    }

    public boolean canConnectEnergy(Level world, BlockPos pos, Direction direction) {
        BlockEntity tile = world.getBlockEntity(pos.relative(direction));
        return !(tile instanceof CableTile) && tile != null && tile.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).isPresent();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
        super.createBlockStateDefinition(builder);
    }
}
