package com.meta.safepill_be.medicine.guide;

public record IngredientGuide(
        String code,
        String ingredientName,
        String riskType,
        String title,
        String summary,
        String detail,
        String sourceName,
        String sourceUrl,
        String reviewedAt) {
}
