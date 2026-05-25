package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicationMatchResultDto {
    private String keyword;
    private List<MedicationMatchCandidateDto> candidates;
}
