package com.meta.safepill_be.cabinet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class IntakeScheduleRequestDto {
    private String takeTime;
    private List<String> daysOfWeek;
    private String dosage;
}
