package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiInteractionItemDto {
    private String itemName;
    private String itemType;
    private List<AiInteractionIngredientDto> ingredients;
    private List<String> intakeTimes;
    private String efficacy;
    private String precautions;
}
