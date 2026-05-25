package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.IntakeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IntakeLogResponseDto {
    private Long logId;
    private Long scheduleId;
    private Long regId;
    private String itemName;
    private String timeSlot;

    // Backward-compatible response field for existing clients.
    private String takeTime;
    private IntakeStatus status;
    private LocalDateTime actualTime;
}
