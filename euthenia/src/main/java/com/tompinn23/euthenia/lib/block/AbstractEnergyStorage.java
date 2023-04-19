package com.tompinn23.euthenia.lib.block;

import com.google.common.primitives.Ints;
import com.tompinn23.euthenia.lib.config.IEnergyConfig;
import com.tompinn23.euthenia.lib.logistics.IRedstoneInteract;
import com.tompinn23.euthenia.lib.logistics.SidedStorage;
import com.tompinn23.euthenia.lib.logistics.Transfer;
import com.tompinn23.euthenia.lib.logistics.energy.EmptyEnergyStorage;
import com.tompinn23.euthenia.lib.logistics.energy.Energy;
import com.tompinn23.euthenia.lib.logistics.energy.SideConfig;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public abstract class AbstractEnergyStorage<T extends Enum<T> & IVariant<T>, C extends IEnergyConfig<T>, B extends AbstractEnergyBlock<T, C, B>> extends AbstractTickingTile<T, B> implements IRedstoneInteract, com.tompinn23.euthenia.lib.logistics.energy.IEnergyStorage {
        protected final SideConfig<AbstractEnergyStorage<T,C,B>> sideConfig = new SideConfig<>(this);
        protected final Energy energy = Energy.create(0);

        private final SidedStorage<LazyOptional<IEnergyStorage>> energyProxies = SidedStorage.create(this::createEnergyProxy);

        private LazyOptional<IEnergyStorage> createEnergyProxy(@Nullable Direction side) {
            return LazyOptional.of(() -> new IEnergyStorage() {
                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return Util.safeInt(AbstractEnergyStorage.this.extractEnergy(maxExtract, simulate, side));
                }

                @Override
                public int getEnergyStored() {
                    return Util.safeInt(AbstractEnergyStorage.this.getEnergy().getStored());
                }

                @Override
                public int getMaxEnergyStored() {
                    return Util.safeInt(AbstractEnergyStorage.this.getEnergy().getMaxEnergyStored());
                }

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    return Util.safeInt(AbstractEnergyStorage.this.receiveEnergy(maxReceive, simulate, side));
                }

                @Override
                public boolean canReceive() {
                    return AbstractEnergyStorage.this.canReceiveEnergy(side);
                }

                @Override
                public boolean canExtract() {
                    return AbstractEnergyStorage.this.canExtractEnergy(side);
                }
            });
        }

        public AbstractEnergyStorage(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            this(type, pos, state, IVariant.EMPTY());
        }

        public AbstractEnergyStorage(BlockEntityType<?> type, BlockPos pos, BlockState state, T variant) {
            super(type, pos, state, variant);
        }

        @Override
        protected void readSync(CompoundTag nbt) {
            this.sideConfig.read(nbt);
            if (!keepEnergy()) {
                this.energy.read(nbt, true, false);
            }
            super.readSync(nbt);
        }

        @Override
        protected CompoundTag writeSync(CompoundTag nbt) {
            this.sideConfig.write(nbt);
            if (!keepEnergy()) {
                this.energy.write(nbt, true, false);
            }
            return super.writeSync(nbt);
        }

        @Override
        public void readStorable(CompoundTag nbt) {
            if (keepEnergy()) {
                this.energy.read(nbt, false, false);
            }
            super.readStorable(nbt);
        }

        @Override
        public CompoundTag writeStorable(CompoundTag nbt) {
            if (keepEnergy()) {
                this.energy.write(nbt, false, false);
            }
            return super.writeStorable(nbt);
        }

        public boolean keepEnergy() {
            return false;
        }

        @Override
        protected void onFirstTick(Level world) {
            super.onFirstTick(world);
            this.energy.setCapacity(getEnergyCapacity());
            this.energy.setTransfer(getEnergyTransfer());
            getSideConfig().init();
            sync();
        }

        protected long extractFromSides(Level world) {
            long extracted = 0;
            if (!isRemote()) {
                for (Direction side : Direction.values()) {
                    if (canExtractEnergy(side)) {
                        long amount = Math.min(getEnergyTransfer(), getEnergy().getStored());
                        var be = level.getBlockEntity(worldPosition.relative(side));
                        var handler = be != null ? be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null) : null;
                        long toExtract = handler != null ? handler.receiveEnergy(Ints.saturatedCast(amount), false) : 0;
                        extracted += extractEnergy(Util.safeInt(toExtract), false, side);
                    }
                }
            }
            return extracted;
        }

        protected long chargeItems(int i) {
            return chargeItems(0, i);
        }

        protected long chargeItems(int i, int j) {
            //long charged = EnvHandler.INSTANCE.chargeItemsInInventory(inv, i, j, getEnergyTransfer(), energy.getStored());
            //energy.consume(charged);
            //return charged;
            long charged = 0;
            for (ItemStack stack : IntStream.range(i, j).mapToObj(inv::getStackInSlot).toList()) {
                if (stack.isEmpty()) continue;
                var cap = stack.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);
                charged += cap.receiveEnergy(Ints.saturatedCast(Math.min(getEnergyTransfer(), energy.getStored() - charged)), false);
            }
            return charged;
        }

        public long extractEnergy(long maxExtract, boolean simulate, @Nullable Direction side) {
            if (!canExtractEnergy(side)) return 0;
            final Energy energy = getEnergy();
            long extracted = Math.min(energy.getStored(), Math.min(energy.getMaxExtract(), maxExtract));
            if (!simulate && extracted > 0) {
                energy.consume(extracted);
                sync();
            }
            return extracted;
        }

        public long receiveEnergy(long maxReceive, boolean simulate, @Nullable Direction side) {
            if (!canReceiveEnergy(side)) return 0;
            final Energy energy = getEnergy();
            long received = Math.min(energy.getEmpty(), Math.min(energy.getMaxReceive(), maxReceive));
            if (!simulate && received > 0) {
                energy.produce(received);
                sync();
            }
            return received;
        }

        public boolean canExtractEnergy(@Nullable Direction side) {
            return side == null || isEnergyPresent(side) && this.sideConfig.getType(side).canExtract;
        }

        public boolean canReceiveEnergy(@Nullable Direction side) {
            return side == null || isEnergyPresent(side) && this.sideConfig.getType(side).canReceive;
        }

        public boolean isEnergyPresent(@Nullable Direction side) {
            return true;
        }

        @Override
        public void onAdded(Level world, BlockState state, BlockState oldState, boolean isMoving) {
            super.onAdded(world, state, oldState, isMoving);
            if (state.getBlock() != oldState.getBlock()) {
                getSideConfig().init();
            }
        }

        protected long getEnergyCapacity() {
            return getConfig().capacity(getVariant());
        }

        protected long getEnergyTransfer() {
            return getConfig().transfer(getVariant());
        }

        protected C getConfig() {
            return getBlock().getConfig();
        }

        public Energy getEnergy() {
            return this.energy;
        }

        @Override
        public void invalidateCaps() {
            super.invalidateCaps();
            this.energyProxies.stream().forEach(LazyOptional::invalidate);
        }



        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
            if(cap == ForgeCapabilities.ENERGY && isEnergyPresent(side)) {
                return this.energyProxies.get(side).cast();
            }
            return super.getCapability(cap, side);
        }

        public Transfer getTransferType() {
            return Transfer.ALL;
        }

        public SideConfig getSideConfig() {
            return this.sideConfig;
        }
    }

