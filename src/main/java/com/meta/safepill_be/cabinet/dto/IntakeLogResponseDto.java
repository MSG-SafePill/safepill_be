package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.IntakeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class IntakeLogResponseDto {
    private Long logId;
    private Long scheduleId;
    private Long regId;
    private String itemName;
    private LocalTime takeTime;
    private String dosage;
    private IntakeStatus status;
    private LocalDateTime actualTime;
}
