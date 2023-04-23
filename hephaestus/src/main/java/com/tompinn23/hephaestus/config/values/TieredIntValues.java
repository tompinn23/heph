package com.tompinn23.hephaestus.config.values;

import com.tompinn23.hephaestus.config.Tier;
import com.tompinn23.hephaestus.config.annotations.IntRange;

/*
    BASIC,
    REFORGED,
    ENERGIZED,
    ENDERFORGED,
 */

public class TieredIntValues {
    @IntRange(min = 0, max = Integer.MAX_VALUE)
    public int basic;

    @IntRange(min = 0, max = Integer.MAX_VALUE)
    public int reforged;
    @IntRange(min = 0, max = Integer.MAX_VALUE)
    public int energized;
    @IntRange(min = 0, max = Integer.MAX_VALUE)
    public int enderforged;

    public TieredIntValues(int basic, int reforged, int energized, int enderforged) {
        this.basic = basic;
        this.reforged = reforged;
        this.energized = energized;
        this.enderforged = enderforged;
    }

    public int get(Tier variant) {
        return switch (variant) {
            case BASIC -> basic;
            case REFORGED -> reforged;
            case ENERGIZED -> energized;
            case ENDERFORGED -> enderforged;
        };
    }

    public TieredIntValues copy(int factor) {
        return new TieredIntValues(basic * factor, reforged * factor, energized * factor, enderforged * factor);
    }
}
