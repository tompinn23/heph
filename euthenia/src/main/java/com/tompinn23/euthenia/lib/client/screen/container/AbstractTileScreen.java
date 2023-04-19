package com.tompinn23.euthenia.lib.client.screen.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tompinn23.euthenia.Euthenia;
import com.tompinn23.euthenia.lib.block.AbstractTile;
import com.tompinn23.euthenia.lib.client.screen.Texture;
import com.tompinn23.euthenia.lib.client.screen.widget.IconButton;
import com.tompinn23.euthenia.lib.container.AbstractTileContainer;
import com.tompinn23.euthenia.lib.logistics.inventory.IInventoryHolder;
import com.tompinn23.euthenia.lib.network.packets.NextRedstoneModePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;


public class AbstractTileScreen<T extends AbstractTile<?, ?> & IInventoryHolder, C extends AbstractTileContainer<T>> extends AbstractContainerScreen<C> {
    protected final T te;
    protected IconButton redStoneButton = IconButton.EMPTY;

    public AbstractTileScreen(C container, Inventory inv, Component title, Texture backGround) {
        super(container, inv, title, backGround);
        this.te = container.te;
    }

    protected void addRedstoneButton(int x, int y) {
        if (hasRedstone()) {
            this.redStoneButton = addRenderableWidget(new IconButton(this.leftPos + this.imageWidth + x + 2, this.topPos + y + 3, Texture.REDSTONE.get(this.te.getRedstoneMode()), b -> {
                Euthenia.NET.toServer(new NextRedstoneModePacket(this.te.getBlockPos()));
                this.te.setRedstoneMode(this.te.getRedstoneMode().next());
            }, this).setTooltip(tooltip -> tooltip.add(this.te.getRedstoneMode().getDisplayName())));
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (hasRedstone()) {
            this.redStoneButton.setTexture(Texture.REDSTONE.get(this.te.getRedstoneMode()));
        }
    }

    @Override
    protected void drawBackground(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(matrix, partialTicks, mouseX, mouseY);
        if (hasRedstone()) {
            Texture.REDSTONE_BTN_BG.draw(matrix, this.redStoneButton.getX() - 2, this.redStoneButton.getY() - 4); //TODO
        }
    }

    protected boolean hasRedstone() { //TODO invert
        return true;
    }
}
