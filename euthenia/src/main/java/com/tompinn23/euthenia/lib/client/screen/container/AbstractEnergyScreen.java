package com.tompinn23.euthenia.lib.client.screen.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tompinn23.euthenia.Euthenia;
import com.tompinn23.euthenia.lib.block.AbstractEnergyStorage;
import com.tompinn23.euthenia.lib.client.screen.Texture;
import com.tompinn23.euthenia.lib.client.screen.widget.IconButton;
import com.tompinn23.euthenia.lib.client.utils.Text;
import com.tompinn23.euthenia.lib.container.AbstractEnergyContainer;
import com.tompinn23.euthenia.lib.logistics.inventory.IInventoryHolder;
import com.tompinn23.euthenia.lib.network.packets.NextEnergyConfigPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.tuple.Pair;


import java.util.List;

public class AbstractEnergyScreen<T extends AbstractEnergyStorage<?, ?, ?> & IInventoryHolder, C extends AbstractEnergyContainer<T>> extends AbstractTileScreen<T, C> {
    protected IconButton[] configButtons = new IconButton[6];
    protected IconButton configButtonAll = IconButton.EMPTY;

    public AbstractEnergyScreen(C container, Inventory inv, Component title, Texture backGround) {
        super(container, inv, title, backGround);
    }

    @Override
    protected void init() {
        super.init();
        if (hasConfigButtons()) {
            addSideConfigButtons(0, 4);
        }
        if (hasRedstoneButton()) {
            addRedstoneButton(0, 31);
        }
    }

    protected void addSideConfigButtons(int x, int y) {
        for (int i = 0; i < 6; i++) {
            final int id = i;
            Pair<Integer, Integer> offset = getSideButtonOffsets(6).get(i);
            int xOffset = offset.getLeft();
            int yOffset = offset.getRight();
            Direction side = Direction.from3DDataValue(i);
            this.configButtons[i] = addRenderableWidget(new IconButton(this.leftPos + xOffset + this.imageWidth + x + 8, this.topPos + yOffset + y + 10, Texture.CONFIG.get(this.te.getSideConfig().getType(side)), button -> {
                Euthenia.NET.toServer(new NextEnergyConfigPacket(id, this.te.getBlockPos()));
                this.te.getSideConfig().nextType(side);
            }, this).setTooltip(tooltip -> {
                tooltip.add(Component.translatable("info.euthenia.facing").append(Text.COLON).withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("info.euthenia.side." + side.getSerializedName()).withStyle(ChatFormatting.DARK_GRAY)));
                tooltip.add(this.te.getSideConfig().getType(side).getDisplayName());
            }));
        }

        this.configButtonAll = addRenderableWidget(new IconButton(this.leftPos + this.imageWidth + x + 14, this.topPos + y + 4, Texture.CONFIG_BTN, button -> {
            Euthenia.NET.toServer(new NextEnergyConfigPacket(6, this.te.getBlockPos()));
            this.te.getSideConfig().nextTypeAll();
        }, this).setTooltip(tooltip -> {
            tooltip.add(Component.translatable("info.euthenia.facing").append(Text.COLON).withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("info.euthenia.all").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(this.te.getSideConfig().getType(Direction.UP).getDisplayName());
        }));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (hasConfigButtons()) {
            for (int i = 0; i < 6; i++) {
                this.configButtons[i].setTexture(Texture.CONFIG.get(this.te.getSideConfig().getType(Direction.from3DDataValue(i))));
            }
        }
    }

    protected List<Pair<Integer, Integer>> getSideButtonOffsets(int spacing) {
        return Lists.newArrayList(Pair.of(0, spacing), Pair.of(0, -spacing), Pair.of(0, 0), Pair.of(spacing, spacing), Pair.of(-spacing, 0), Pair.of(spacing, 0));
    }

    @Override
    protected void drawBackground(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(matrix, partialTicks, mouseX, mouseY);
        if (hasConfigButtons()) {
            Texture.CONFIG_BTN_BG.draw(matrix, this.configButtons[1].getX() - 8, this.configButtons[1].getY() - 4);
        }
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        final List<Rect2i> extraAreas = super.getExtraAreas();
        if (hasConfigButtons()) {
            extraAreas.add(toRectangle2d(this.configButtons[1].getX() - 8, this.configButtons[1].getY() - 4, Texture.CONFIG_BTN_BG));
        }
        return extraAreas;
    }

    protected boolean hasConfigButtons() {
        return true;
    }

    protected boolean hasRedstoneButton() {
        return true;
    }
}