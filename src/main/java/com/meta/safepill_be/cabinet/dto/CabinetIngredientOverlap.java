package com.meta.safepill_be.cabinet.dto;

import java.util.List;

public record CabinetIngredientOverlap(String code, String ingredientName, List<RelatedItem> relatedItems) {
    public record RelatedItem(Long regId, String itemName) {}
}
