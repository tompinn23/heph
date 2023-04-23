package com.tompinn23.hephaestus.tile;

import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.hephaestus.cables.EnergyCableNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;

public class EnergyCableTile extends CableTile<EnergyCableNetwork> {


    public EnergyCableTile(BlockPos pos, BlockState state) {
        this(pos, state, IVariant.EMPTY());
    }

    public EnergyCableTile(BlockPos pos, BlockState state, IVariant.Single variant) {
        super(pos, state, variant);
    }





    @Override
    public EnergyCableNetwork newNetwork() {
        return new EnergyCableNetwork(this.level);
    }

    @Override
    public Class<EnergyCableNetwork> getNetworkType() {
        return EnergyCableNetwork.class;
    }
}
