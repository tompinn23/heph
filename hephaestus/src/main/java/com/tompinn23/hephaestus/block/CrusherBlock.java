package com.tompinn23.hephaestus.block;

import com.tompinn23.hephaestus.Hephaestus;
import com.tompinn23.hephaestus.config.IMachineConfig;
import com.tompinn23.hephaestus.config.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrusherBlock extends AbstractMachineBlock<Tier, CrusherBlock> {
    public CrusherBlock(Properties properties, Tier variant) {
        super(properties, variant);
    }

    @Override
    public IMachineConfig<Tier> getConfig() {
        return Hephaestus.getConfig().machines.crusher;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        //return new CrusherTile(pos, state, this.variant);
        return null;
    }
}
