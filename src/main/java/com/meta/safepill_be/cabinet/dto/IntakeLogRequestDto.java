package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.IntakeStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class IntakeLogRequestDto {
    private IntakeStatus status;
    private LocalDateTime actualTime;
}
