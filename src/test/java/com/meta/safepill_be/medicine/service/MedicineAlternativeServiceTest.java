package com.meta.safepill_be.medicine.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicineAlternativeServiceTest {
    @Test
    void aiSuggestionNearlyIdenticalToTargetIsTreatedAsSameProduct() {
        assertTrue(MedicineAlternativeService.isSameProduct("써스펜", "써스펜데이정"));
        assertTrue(MedicineAlternativeService.isSameProduct("한신타미놀캅셀", "한신타미놀캅셀(아세트아미노펜)"));
    }

    @Test
    void differentProductsAreNotSameProduct() {
        assertFalse(MedicineAlternativeService.isSameProduct("부스코판당의정", "써스펜데이정"));
        assertFalse(MedicineAlternativeService.isSameProduct("", "써스펜데이정"));
    }
}
