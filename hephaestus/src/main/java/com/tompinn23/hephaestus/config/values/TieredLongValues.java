package com.tompinn23.hephaestus.config.values;

import com.tompinn23.hephaestus.config.Tier;
import com.tompinn23.hephaestus.config.annotations.LongRange;

/*
    BASIC,
            REFORGED,
            ENERGIZED,
            ENDERFORGED,
            */

public class TieredLongValues {
    @LongRange(min = 0, max = Long.MAX_VALUE)
    public long basic;

    @LongRange(min = 0, max = Long.MAX_VALUE)
    public long reforged;

    @LongRange(min = 0, max = Long.MAX_VALUE)
    public long energized;

    @LongRange(min = 0, max = Long.MAX_VALUE)
    public long enderforged;

    public TieredLongValues(long basic, long reforged, long energized, long enderforged) {
        this.basic = basic;
        this.reforged = reforged;
        this.energized = energized;
        this.enderforged = enderforged;
    }

    public long get(Tier t) {
        return switch (t) {
            case BASIC -> basic;
            case REFORGED -> reforged;
            case ENERGIZED -> energized;
            case ENDERFORGED -> enderforged;
        };
    }

    public TieredLongValues copy(long factor) {
        return new TieredLongValues(basic * factor, reforged * factor, energized * factor, enderforged * factor);
    }
}
