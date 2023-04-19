package com.tompinn23.euthenia.lib.container;

import com.tompinn23.euthenia.lib.block.AbstractTile;
import com.tompinn23.euthenia.lib.logistics.inventory.IInventoryHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;

public abstract class AbstractTileContainer<T extends AbstractTile<?, ?> & IInventoryHolder> extends AbstractContainer {
    public final T te;

    public AbstractTileContainer(@Nullable MenuType<?> containerType, int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerType, id, inventory, getInventory(inventory.player, buffer.readBlockPos()));
    }

    public AbstractTileContainer(@Nullable MenuType<?> type, int id, Inventory inventory, T te) {
        super(type, id, inventory);
        this.te = te;
        init(inventory, te);
        this.te.setContainerOpen(true);
    }

    @Override
    protected final void init(Inventory inventory) {
        super.init(inventory);
    }

    protected void init(Inventory inventory, T te) {

    }

    @SuppressWarnings("unchecked")
    protected static <T extends AbstractTile<?, ?>> T getInventory(Player player, BlockPos pos) {
        BlockEntity tile = player.level.getBlockEntity(pos);
        if (tile instanceof AbstractTile<?, ?>)
            return (T) tile;
        // What the hell is this?
        return (T) new AbstractTile(BlockEntityType.SIGN, pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.te.setContainerOpen(false);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack1 = slot.getItem();
            stack = stack1.copy();
            int size = this.te.getInventory().getSlots();
            if (index < size) {
                if (!moveItemStackTo(stack1, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack1, 0, size, false)) {
                return ItemStack.EMPTY;
            }
            if (stack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
                slot.onTake(this.player, stack);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }
}
