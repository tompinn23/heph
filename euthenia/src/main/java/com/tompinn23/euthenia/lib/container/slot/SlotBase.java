package com.tompinn23.euthenia.lib.container.slot;

import com.tompinn23.euthenia.lib.logistics.inventory.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class SlotBase extends SlotItemHandler {
    private boolean enabled = true;

    public SlotBase(ItemStackHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        if (this.getItemHandler() instanceof Inventory) {
            return !((Inventory) getItemHandler()).extractItemFromSlot(index, 1, true).isEmpty();
        } else return super.mayPickup(player);
    }

    @Nonnull
    @Override
    public ItemStack remove(int amount) {
        if (this.getItemHandler() instanceof Inventory) {
            return ((Inventory) getItemHandler()).extractItemFromSlot(index, amount, false);
        } else return super.remove(amount);
    }

    @Override
    public boolean isActive() {
        return this.enabled;
    }

    public SlotBase setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}