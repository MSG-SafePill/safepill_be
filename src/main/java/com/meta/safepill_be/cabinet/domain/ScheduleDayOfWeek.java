package com.meta.safepill_be.cabinet.domain;

import java.time.DayOfWeek;

public enum ScheduleDayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
    EVERYDAY;

    public boolean matches(DayOfWeek dayOfWeek) {
        return this == EVERYDAY || this.name().equals(dayOfWeek.name());
    }
}
