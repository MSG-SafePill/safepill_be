package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiInteractionIngredientDto {
    private String name;
    private String dosage;
}
