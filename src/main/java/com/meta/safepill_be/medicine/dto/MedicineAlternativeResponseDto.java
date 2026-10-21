package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineAlternativeResponseDto {
    private Target target;
    private List<MedicineAlternativeDto> alternatives;
    private boolean aiUnavailable;

    @Getter
    @Builder
    public static class Target {
        private Long id;
        private String medicineName;
        private String manufacturer;
        private List<String> ingredients;
        private boolean permitActive;
        private String permitStatus;
        private String cancelDate;
    }
}
