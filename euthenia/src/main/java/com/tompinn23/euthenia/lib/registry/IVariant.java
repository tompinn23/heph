package com.tompinn23.euthenia.lib.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public interface IVariant<V extends Enum<V> & IVariant<V>> {
    V[] getVariants();

    default String name() { return ((Enum<?>) this).name().toLowerCase(Locale.UK); }

    default V read(CompoundTag nbt, String key) { return getVariants()[nbt.getInt(key)]; }

    default CompoundTag write(CompoundTag nbt, V v, String key) {
        nbt.putInt(key, ((Enum<?>) this).ordinal());
        return nbt;
    }

    default boolean isEmpty() { return this instanceof IVariant.Single || getVariants().length == 0; }

    @SuppressWarnings("unchecked")
    static <T extends IVariant> T EMPTY() { return (T) Single.SINGLE; }
    int ordinal();

    Component getFormatted();

    enum Single implements IVariant<Single> {
        SINGLE;

        @Override
        public Single[] getVariants() {
            return new Single[0];
        }

        @Override
        public Component getFormatted() {
            return Component.literal("");
        }
    }
}
