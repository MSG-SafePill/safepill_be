package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class IntakeScheduleResponseDto {
    private Long scheduleId;
    private Long regId;
    private String itemName;
    private LocalTime takeTime;
    private ScheduleDayOfWeek dayOfWeek;
    private String dosage;
}
