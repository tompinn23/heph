package com.tompinn23.hephaestus.config;

import com.tompinn23.hephaestus.config.values.TieredIntValues;
import com.tompinn23.hephaestus.config.values.TieredLongValues;

public class Defaults {
    private static TieredIntValues baseScale() {
        return new TieredIntValues(1, 2, 4, 8);
    }

    public static TieredIntValues defaultVoltages() {
        return baseScale().copy(32);
    }

    public static TieredIntValues defaultAmps() {
        return baseScale().copy(4);
    }

    public static TieredLongValues defaultCapacities() {
        return new TieredLongValues(8000, 16000, 32000, 64000);
    }
}
