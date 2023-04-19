package com.tompinn23.euthenia.lib.container;

import com.tompinn23.euthenia.lib.block.AbstractTile;
import com.tompinn23.euthenia.lib.logistics.inventory.IInventoryHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;

public abstract class AbstractEnergyContainer <T extends AbstractTile<?, ?> & IInventoryHolder> extends AbstractTileContainer<T> {
    public AbstractEnergyContainer(@Nullable MenuType<?> containerType, int id, Inventory inventory, FriendlyByteBuf buffer) {
        super(containerType, id, inventory, buffer);
    }

    public AbstractEnergyContainer(@Nullable MenuType<?> type, int id, Inventory inventory, T te) {
        super(type, id, inventory, te);
    }
}
