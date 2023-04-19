package com.tompinn23.euthenia.lib.logistics.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidTank {
    protected Predicate<FluidStack> validator;
    @NotNull
    protected FluidStack fluid = FluidStack.EMPTY;
    protected int capacity;

    public FluidTank(int capacity)
    {
        this(capacity, e -> true);
    }

    public FluidTank(int capacity, Predicate<FluidStack> validator)
    {
        this.capacity = capacity;
        this.validator = validator;
    }

    public FluidTank setCapacity(int capacity)
    {
        this.capacity = capacity;
        return this;
    }

    public FluidTank setValidator(Predicate<FluidStack> validator)
    {
        if (validator != null) {
            this.validator = validator;
        }
        return this;
    }

    public boolean isFluidValid(FluidStack stack)
    {
        return validator.test(stack);
    }

    public long getCapacity()
    {
        return capacity;
    }

    @NotNull
    public FluidStack getFluid()
    {
        return fluid;
    }

    public long getFluidAmount()
    {
        return fluid.getAmount();
    }

    public FluidTank readFromNBT(CompoundTag nbt) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("tank"));
        setFluid(fluid);
        return this;
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        if (!fluid.isEmpty()) {
            nbt.put("tank", fluid.writeToNBT(new CompoundTag()));
        }

        return nbt;
    }

    public int getTanks() {

        return 1;
    }

    @NotNull
    public FluidStack getFluidInTank(int tank) {

        return getFluid();
    }

    public long getTankCapacity(int tank) {

        return getCapacity();
    }

    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {

        return isFluidValid(stack);
    }

    public long fill(FluidStack resource, boolean simulate)
    {
        if (resource.isEmpty() || !isFluidValid(resource))
        {
            return 0;
        }
        if (simulate)
        {
            if (fluid.isEmpty())
            {
                return Math.min(capacity, resource.getAmount());
            }
            if (!fluid.isFluidEqual(resource))
            {
                return 0;
            }
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        if (fluid.isEmpty())
        {
            fluid = resource.copy();
            fluid.setAmount(Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            return fluid.getAmount();
        }
        if (!fluid.isFluidEqual(resource))
        {
            return 0;
        }
        long filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled)
        {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        }
        else
        {
            fluid.setAmount(capacity);
        }
        if (filled > 0)
            onContentsChanged();
        return filled;
    }

    @NotNull
    public FluidStack drain(FluidStack resource, boolean simulate)
    {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid))
        {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), simulate);
    }

    @NotNull
    public FluidStack drain(int maxDrain, boolean simulate)
    {
        int drained = maxDrain;
        if (fluid.getAmount() < drained)
        {
            drained = fluid.getAmount();
        }
        FluidStack stack = fluid.copy();
        stack.setAmount(drained);
        if (!simulate && drained > 0)
        {
            fluid.shrink(drained);
            onContentsChanged();
        }
        return stack;
    }

    protected void onContentsChanged()
    {

    }

    public void setFluid(FluidStack stack)
    {
        this.fluid = stack;
    }

    public boolean isEmpty()
    {
        return fluid.isEmpty();
    }

    public long getSpace()
    {
        return Math.max(0, capacity - fluid.getAmount());
    }

}
