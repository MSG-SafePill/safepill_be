package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.dto.CabinetItemResponseDto;
import com.meta.safepill_be.medicine.guide.IngredientGuideService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CabinetIngredientOverlapsTest {
    private CabinetItemResponseDto item(long regId, long itemId, ItemType type, String... names) {
        return CabinetItemResponseDto.builder().regId(regId).itemId(itemId).type(type)
                .itemName("제품 " + regId).ingredientGuide(new IngredientGuideService().assess(List.of(names))).build();
    }

    @Test void aliasesAcrossProductTypesMatchWithoutCountingRepeatedIngredients() {
        var result = CabinetIngredientOverlaps.enrich(List.of(
                item(1, 10, ItemType.MEDICINE, "아스코르브산", "비타민C"),
                item(2, 10, ItemType.SUPPLEMENT, "비타민 C")));
        assertEquals(1, result.get(0).getIngredientOverlaps().size());
        var overlap = result.get(0).getIngredientOverlaps().get(0);
        assertEquals("VITAMIN_C", overlap.code());
        assertEquals(2L, overlap.relatedItems().get(0).regId());
        assertEquals(1L, result.get(1).getIngredientOverlaps().get(0).relatedItems().get(0).regId());
    }

    @Test void doesNotCountSameProductOrUnrecognizedIngredients() {
        var result = CabinetIngredientOverlaps.enrich(List.of(
                item(1, 10, ItemType.MEDICINE, "칼슘", "칼슘"),
                item(2, 10, ItemType.MEDICINE, "칼슘"),
                item(3, 11, ItemType.MEDICINE, "로수바스타틴칼슘"),
                item(4, 12, ItemType.MEDICINE)));
        assertTrue(result.stream().allMatch(i -> i.getIngredientOverlaps().isEmpty()));
    }

    @Test void recalculatesAfterRemovalAndNeverMutatesInput() {
        var first = item(1, 10, ItemType.MEDICINE, "모르핀");
        var second = item(2, 11, ItemType.MEDICINE, "모르핀황산염수화물");
        assertFalse(CabinetIngredientOverlaps.enrich(List.of(first, second)).get(0).getIngredientOverlaps().isEmpty());
        assertTrue(first.getIngredientOverlaps().isEmpty());
        assertTrue(CabinetIngredientOverlaps.enrich(List.of(first)).get(0).getIngredientOverlaps().isEmpty());
        assertTrue(CabinetIngredientOverlaps.enrich(List.of()).isEmpty());
    }
}
