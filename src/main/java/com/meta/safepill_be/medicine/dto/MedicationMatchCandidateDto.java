package com.meta.safepill_be.medicine.dto;

import com.meta.safepill_be.cabinet.domain.ItemType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicationMatchCandidateDto {
    private ItemType itemType;
    private Long itemId;
    private String itemName;
    private String manufacturer;
    private double score;
    private boolean registered;
}
