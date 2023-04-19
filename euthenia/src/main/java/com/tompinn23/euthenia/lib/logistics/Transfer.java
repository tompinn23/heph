package com.tompinn23.euthenia.lib.logistics;

import com.tompinn23.euthenia.lib.util.Text;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum Transfer {
    ALL(true, true, ChatFormatting.DARK_GRAY),
    EXTRACT(true, false, ChatFormatting.DARK_GRAY),
    RECEIVE(false, true, ChatFormatting.DARK_GRAY),
    NONE(false, false, ChatFormatting.DARK_RED);

    public final boolean canExtract;
    public final boolean canReceive;
    private final ChatFormatting color;

    Transfer(boolean canExtract, boolean canReceive, ChatFormatting color) {
        this.canExtract = canExtract;
        this.canReceive = canReceive;
        this.color = color;
    }

    public Transfer next(Transfer type) {
        if (ALL.equals(type)) {
            int i = ordinal();
            if (i < 3) i++;
            else i = 0;
            return values()[i];
        } else if (EXTRACT.equals(type)) {
            return !NONE.equals(this) ? NONE : EXTRACT;
        } else if (RECEIVE.equals(type)) {
            return !NONE.equals(this) ? NONE : RECEIVE;
        }
        return NONE;
    }

    public Component getDisplayName() {
        return Component.translatable("info.euthenia.io.mode").append(Text.COLON).withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("info.euthenia.io.mode." + name().toLowerCase()).withStyle(this.color));
    }

    public Component getDisplayName2() {
        return Component.translatable("info.euthenia.io.mode").append(Text.COLON).withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("info.euthenia.io.mode." + translate(name().toLowerCase(Locale.ENGLISH))).withStyle(this.color));
    }

    private String translate(String s) {
        return s.equals("extract") ? "push" : s.equals("receive") ? "pull" : s;
    }
}
