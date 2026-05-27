package com.meta.safepill_be.cabinet.dto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IntakeScheduleResponseDto {
    private Long scheduleId;
    private Long regId;
    private String itemName;
    private String timeSlot;

    // Backward-compatible response field for existing clients.
    private String takeTime;
}
