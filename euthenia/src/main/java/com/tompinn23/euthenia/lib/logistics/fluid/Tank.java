package com.tompinn23.euthenia.lib.logistics.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public class Tank extends FluidTank {
    private Runnable changed = () -> {
    };
    private Object platformWrapper;

    public Tank(int capacity) {
        this(capacity, e -> true);
    }

    public Tank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    public FluidTank load(CompoundTag nbt, String key, boolean capacity) {
        CompoundTag compound = nbt.getCompound(key);
        FluidTank tank = super.readFromNBT(compound);
        if(capacity) {
            tank.setCapacity(compound.getInt("capacity"));
        }
        return tank;
    }

    public CompoundTag save(CompoundTag nbt, String key, boolean capacity) {
        CompoundTag compound = super.writeToNBT(new CompoundTag());
        if(capacity) {
            compound.putInt("capacity", this.capacity);
        }
        nbt.put(key, compound);
        return nbt;
    }


    public Tank validate(Predicate<FluidStack> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public Tank setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public Tank setChange(Runnable changed) {
        this.changed = changed;
        return this;
    }

    private boolean sendUpdates = true;

    public void setSendUpdates(boolean sendUpdates) {
        this.sendUpdates = sendUpdates;
    }

    @Override
    public void onContentsChanged() {
        if (sendUpdates) {
            this.changed.run();
        }
    }
}
