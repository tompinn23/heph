package com.tompinn23.euthenia.lib.logistics;

import com.tompinn23.euthenia.lib.util.Text;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum Redstone {
    IGNORE(ChatFormatting.DARK_GRAY),
    ON(ChatFormatting.RED),
    OFF(ChatFormatting.DARK_RED);

    private final ChatFormatting color;

    Redstone(ChatFormatting color) {
        this.color = color;
    }

    public Redstone next() {
        int i = ordinal() + 1;
        return values()[i > 2 ? 0 : i];
    }

    public Component getDisplayName() {
        return Component.translatable("info.balnor.redstone").append(Text.COLON).withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("info.balnor." + name().toLowerCase()).withStyle(this.color));
    }
}
