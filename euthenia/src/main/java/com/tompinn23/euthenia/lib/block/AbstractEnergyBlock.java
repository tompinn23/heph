package com.tompinn23.euthenia.lib.block;

import com.tompinn23.euthenia.api.energy.IEnergyConnector;
import com.tompinn23.euthenia.lib.config.IConfigHolder;
import com.tompinn23.euthenia.lib.config.IEnergyConfig;
import com.tompinn23.euthenia.lib.item.EnergyBlockItem;
import com.tompinn23.euthenia.lib.item.IEnergyItemProvider;
import com.tompinn23.euthenia.lib.logistics.Transfer;
import com.tompinn23.euthenia.lib.logistics.energy.Energy;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.util.Text;
import com.tompinn23.euthenia.lib.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractEnergyBlock<T extends Enum<T> & IVariant<T>, C extends IEnergyConfig<T>, B extends AbstractEnergyBlock<T, C, B>> extends AbstractBlock<T, B> implements IConfigHolder<T, C>, IEnergyItemProvider {
    public AbstractEnergyBlock(Properties props) {
        this(props, IVariant.EMPTY());
    }

    public AbstractEnergyBlock(Properties properties, T variant) {
        super(properties, variant);
    }

    @Override
    public BlockItem getBlockItem(Item.Properties props) {
        return new EnergyBlockItem(this, props);
    }

    @Override
    public Component getDisplayName(ItemStack stack) {
        return super.getDisplayName(stack).copy().append(Text.COLON).append(getVariant().getFormatted());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof AbstractEnergyStorage) {
            return ((AbstractEnergyStorage) tile).getEnergy().toComparatorPower();
        }
        return super.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if(checkValidEnergySide()) {
            Direction side = state.getValue(BlockStateProperties.FACING);
            BlockPos offsetPos = pos.relative(side);
            var be = world.getBlockEntity(offsetPos);
            return world.getBlockState(offsetPos).getBlock() instanceof IEnergyConnector ||
                    world instanceof Level level && be != null && be.getCapability(ForgeCapabilities.ENERGY, side).isPresent();
        }
        return super.canSurvive(state, world, pos);
    }

     protected boolean checkValidEnergySide() {
        return false;
     }

    @Override
    public boolean isChargeable(ItemStack stack) {
         return getTransferType().canReceive;
    }

    public Transfer getTransferType() {
        return Transfer.ALL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Energy.ifPresent(stack, energy -> {
            addEnergyInfo(stack, energy, tooltip);
            addEnergyTransferInfo(stack, energy, tooltip);
            additionalEnergyInfo(stack, energy, tooltip);
        });
    }

    public void addEnergyInfo(ItemStack stack, Energy.Item storage, List<Component> tooltip) {
        if (storage.getCapacity() > 0)
            tooltip.add(Component.translatable("info.euthenia.stored").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(Component.translatable("info.euthenia.fe.stored", Util.addCommas(storage.getStored()), Util.numFormat(storage.getCapacity())).withStyle(ChatFormatting.DARK_GRAY)));
    }

    public void addEnergyTransferInfo(ItemStack stack, Energy.Item storage, List<Component> tooltip) {
        long ext = storage.getMaxExtract();
        long re = storage.getMaxReceive();
        if (ext + re > 0) {
            if (ext == re) {
                tooltip.add(Component.translatable("info.euthenia.max.io").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(Component.literal(Util.numFormat(ext)).append(Component.translatable("info.euthenia.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                if (ext > 0)
                    tooltip.add(Component.translatable("info.euthenia.max.extract").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(Component.literal(Util.numFormat(ext)).append(Component.translatable("info.euthenia.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
                if (re > 0)
                    tooltip.add(Component.translatable("info.euthenia.max.receive").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(Component.literal(Util.numFormat(re)).append(Component.translatable("info.euthenia.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
    }

    public void additionalEnergyInfo(ItemStack stack, Energy.Item energy, List<Component> tooltip) {
    }
}
