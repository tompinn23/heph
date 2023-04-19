package com.tompinn23.euthenia.lib.container.slot;


import com.tompinn23.euthenia.lib.client.screen.Texture;

public interface ITexturedSlot<T extends ITexturedSlot> {
    Texture getOverlay();

    T setOverlay(Texture overlay);

    Texture getBackground2();

    T setBackground(Texture background);
}