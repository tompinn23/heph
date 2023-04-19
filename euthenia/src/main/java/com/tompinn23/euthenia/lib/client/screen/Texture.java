package com.tompinn23.euthenia.lib.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tompinn23.euthenia.lib.logistics.Redstone;
import com.tompinn23.euthenia.lib.logistics.Transfer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class Texture extends GuiComponent {
    private static final Builder BUILDER = new Builder("balnor");
    public static final Texture EMPTY = BUILDER.make("empty", 0, 0, 0, 0);

    // Side config
    public static final Map<Transfer, Texture> CONFIG = new HashMap<>();
    public static final Texture CONFIG_BTN_BG = BUILDER.make("container/button_ov", 23, 25, 0, 0);
    public static final Texture CONFIG_BTN = BUILDER.make("container/button_ov", 5, 5, 23, 16);
    public static final Texture CONFIG_BTN_ALL = BUILDER.make("container/button_ov", 5, 5, 28, 16);
    public static final Texture CONFIG_BTN_OUT = BUILDER.make("container/button_ov", 5, 5, 33, 16);
    public static final Texture CONFIG_BTN_IN = BUILDER.make("container/button_ov", 5, 5, 38, 16);
    public static final Texture CONFIG_BTN_OFF = BUILDER.make("container/button_ov", 5, 5, 43, 16);

    // Redstone mode
    public static final Map<Redstone, Texture> REDSTONE = new HashMap<>();
    public static final Texture REDSTONE_BTN_BG = BUILDER.make("container/button_ov", 15, 16, 23, 0);
    public static final Texture REDSTONE_BTN_IGNORE = BUILDER.make("container/button_ov", 9, 8, 38, 0);
    public static final Texture REDSTONE_BTN_OFF = BUILDER.make("container/button_ov", 9, 8, 47, 0);
    public static final Texture REDSTONE_BTN_ON = BUILDER.make("container/button_ov", 9, 8, 38, 8);

    private final ResourceLocation location;
    private final int width, height;
    private final int u, v;
    private final int tw, th;

    public Texture(ResourceLocation location, int width, int height) {
        this(location, width, height, 0, 0, width, height);
    }

    public Texture(ResourceLocation location, int width, int height, int u, int v) {
        this(location, width, height, u, v, 256, 256);
    }

    public Texture(ResourceLocation location, int width, int height, int u, int v, int dim) {
        this(location, width, height, u, v, dim, dim);
    }

    public Texture(ResourceLocation location, int width, int height, int u, int v, int tw, int th) {
        this.location = location;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.tw = tw;
        this.th = th;
    }

    public void drawScalableW(PoseStack matrix, float size, int x, int y) {
        scaleW((int) (size * this.width)).draw(matrix, x, y);
    }

    public void drawScalableH(PoseStack matrix, float size, int x, int y) {
        int i = (int) (size * this.height);
        scaleH(i).moveV(this.height - i).draw(matrix, x, y + this.height - i);
    }

    public void draw(PoseStack matrix, int x, int y) {
        if (!isEmpty()) {
            bindTexture(getLocation());
            blit(matrix, x, y, getU(), getV(), getWidth(), getHeight(), this.tw, this.th);
        }
    }

    public void draw(PoseStack matrix, int x, int y, int width, int height) {
        if(!isEmpty()) {
            bindTexture(getLocation());
            blit(matrix, x, y, getU(), getV(), width == -1 ? getWidth() : width, height == -1 ? getHeight() : height, this.tw, this.th);
        }
    }

    public void bindTexture(ResourceLocation guiTexture) {
        RenderSystem.setShaderTexture(0, guiTexture);
    }

    public Texture addW(int width) {
        return scaleW(this.width + width);
    }

    public Texture addH(int height) {
        return scaleH(this.height + height);
    }

    public Texture remW(int width) {
        return scaleW(this.width - width);
    }

    public Texture remH(int height) {
        return scaleH(this.height - height);
    }

    public Texture scaleW(int width) {
        return scale(width, this.height);
    }

    public Texture scaleH(int height) {
        return scale(this.width, height);
    }

    public Texture scale(int width, int height) {
        return new Texture(this.location, width, height, this.u, this.v);
    }

    public Texture moveU(int u) {
        return move(u, 0);
    }

    public Texture moveV(int v) {
        return move(0, v);
    }

    public Texture move(int u, int v) {
        return new Texture(this.location, this.width, this.height, this.u + u, this.v + v);
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getU(int i) {
        return this.u + i;
    }

    public int getV(int i) {
        return this.v + i;
    }

    public int getU() {
        return this.u;
    }

    public int getV() {
        return this.v;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isMouseOver(int x, int y, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
    }

    static {
//        CONFIG.put(Transfer.ALL, CONFIG_BTN_ALL);
//        CONFIG.put(Transfer.EXTRACT, CONFIG_BTN_OUT);
//        CONFIG.put(Transfer.RECEIVE, CONFIG_BTN_IN);
//        CONFIG.put(Transfer.NONE, CONFIG_BTN_OFF);
        REDSTONE.put(Redstone.IGNORE, REDSTONE_BTN_IGNORE);
        REDSTONE.put(Redstone.ON, REDSTONE_BTN_ON);
        REDSTONE.put(Redstone.OFF, REDSTONE_BTN_OFF);
    }

    public static class Builder {
        private final String id;

        public Builder(String id) {
            this.id = id;
        }

        public Texture make(String path, int width, int height) {
            return new Texture(new ResourceLocation(this.id, "textures/gui/" + path + ".png"), width, height);
        }

        public Texture make(String path, int width, int height, int u, int v, int w, int h) {
            return new Texture(new ResourceLocation(this.id, "textures/gui/" + path + ".png"), width, height, u, v, w, h);
        }

        public Texture make(String path, int width, int height, int u, int v, int d) {
            return new Texture(new ResourceLocation(this.id, "textures/gui/" + path + ".png"), width, height, u, v, d);
        }

        public Texture make(String path, int width, int height, int u, int v) {
            return new Texture(new ResourceLocation(this.id, "textures/gui/" + path + ".png"), width, height, u, v);
        }
    }
}
