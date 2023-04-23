package com.tompinn23.hephaestus.config.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface LongRange {
    long min();
    long max();
}

