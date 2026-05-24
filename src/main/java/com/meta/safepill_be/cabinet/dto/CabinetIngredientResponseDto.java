package com.meta.safepill_be.cabinet.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CabinetIngredientResponseDto {
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal dosage;
    private String unit;
}
