package com.tompinn23.hephaestus.config;

import com.tompinn23.euthenia.lib.client.utils.Text;
import com.tompinn23.euthenia.lib.registry.IVariant;
import net.minecraft.network.chat.Component;
public enum Tier implements IVariant<Tier> {
    BASIC(0xA7A7A7, "hephaestus.tier.basic"),
    REFORGED(0xA3AB9F, "hephaestus.tier.reforged"),
    ENERGIZED(0xBBA993, "hephaestus.tier.energized"),
    ENDERFORGED(0xE4B040, "hephaestus.tier.enderforged");

    private final int color;
    private final String name;

    Tier(int color, String name) {
        this.color = color;
        this.name = name;
    }

    @Override
    public Tier[] getVariants() {
        return values();
    }

    public static Tier[] getNormalVariants() {
        return new Tier[]{BASIC, REFORGED, ENERGIZED, ENDERFORGED};
    }

    public int getColor() {
        return color;
    }
    public Component getFormatted() {
        return Component.translatable(this.name).withStyle(Text.color(color));
    }
}