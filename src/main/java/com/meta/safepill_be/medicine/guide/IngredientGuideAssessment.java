package com.meta.safepill_be.medicine.guide;

import java.util.List;

public record IngredientGuideAssessment(
        String status,
        List<IngredientGuide> warnings,
        List<String> unmatchedIngredients) {
}
