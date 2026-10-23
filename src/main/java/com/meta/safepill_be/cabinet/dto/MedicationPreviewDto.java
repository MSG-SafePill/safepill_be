package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.medicine.guide.IngredientGuideAssessment;
import java.util.List;

public record MedicationPreviewDto(ItemType type, Long itemId, String itemName,
        boolean alreadyRegistered, IngredientGuideAssessment ingredientGuide,
        List<CabinetIngredientOverlap> ingredientOverlaps, long unverifiedCabinetItems) {}
