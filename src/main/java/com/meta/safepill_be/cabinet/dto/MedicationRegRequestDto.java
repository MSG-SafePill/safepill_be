package com.meta.safepill_be.cabinet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicationRegRequestDto {
    private String type;
    private Long itemId;
    private Integer supplyDays;
}
