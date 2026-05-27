package com.meta.safepill_be.cabinet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IntakeScheduleRequestDto {
    private String timeSlot;

    // Backward-compatible input from existing clients. Persisted as timeSlot.
    private String takeTime;

    public String resolveTimeSlot() {
        if (timeSlot != null && !timeSlot.isBlank()) {
            return timeSlot;
        }
        return takeTime;
    }
}
