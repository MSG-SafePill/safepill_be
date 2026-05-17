package com.meta.safepill_be.vision.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VisionIngredientDto {
    private Long id;
    private String name;
    private BigDecimal dosage;
    private String unit;
}
