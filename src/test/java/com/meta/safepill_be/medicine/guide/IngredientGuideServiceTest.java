package com.meta.safepill_be.medicine.guide;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IngredientGuideServiceTest {
    private final IngredientGuideService service = new IngredientGuideService();

    @Test
    void selectedFiveHaveDistinctRiskTypesAndEvidence() {
        var result = service.assess(List.of("칼슘", "비타민C", "아세트아미노펜", "마그네슘", "모르핀"));
        assertEquals("AVAILABLE", result.status());
        assertEquals(5, result.warnings().size());
        assertEquals(1, result.warnings().stream().filter(g -> g.riskType().equals("DEPENDENCE")).count());
        assertEquals(3, result.warnings().stream().filter(g -> g.riskType().equals("EXCESS_INTAKE")).count());
        for (var guide : result.warnings()) {
            assertTrue(guide.sourceUrl().startsWith("https://"));
            assertFalse(guide.detail().isBlank());
            assertFalse(guide.reviewedAt().isBlank());
        }
    }

    @Test
    void normalizesWhitespaceCaseAndWidthAndDeduplicatesAliases() {
        var result = service.assess(List.of(" 비타민 C ", "Ｖｉｔａｍｉｎ　Ｃ", "Ascorbic Acid",
                "모르핀황산염수화물", "MORPHINE SULFATE HYDRATE"));
        assertEquals("AVAILABLE", result.status());
        assertEquals(List.of("VITAMIN_C", "MORPHINE"),
                result.warnings().stream().map(IngredientGuide::code).toList());
    }

    @Test
    void doesNotMatchUnrelatedSaltsSubstringsProductsOrDosageText() {
        var result = service.assess(List.of("로수바스타틴칼슘", "스테아르산마그네슘",
                "아포모르핀", "hydromorphone", "타이레놀", "아세트아미노펜 500mg"));
        assertEquals("NO_REGISTERED_GUIDE", result.status());
        assertTrue(result.warnings().isEmpty());
        assertEquals(6, result.unmatchedIngredients().size());
    }

    @Test
    void reportsPartialCoverageForCombinationProducts() {
        var result = service.assess(Arrays.asList("APAP", "카페인", null, "카페인"));
        assertEquals("PARTIAL", result.status());
        assertEquals("ACETAMINOPHEN", result.warnings().get(0).code());
        assertEquals(List.of("카페인", "성분명 없음"), result.unmatchedIngredients());
    }

    @Test
    void missingDataIsNotReportedAsSafeOrCovered() {
        assertEquals("NO_INGREDIENT_DATA", service.assess(null).status());
        assertEquals("NO_INGREDIENT_DATA", service.assess(List.of()).status());
        assertEquals("NO_INGREDIENT_DATA", service.assess(Arrays.asList(null, " ")).status());
    }

    @Test
    void supportsExplicitMineralForms() {
        assertEquals(List.of("CALCIUM", "MAGNESIUM"),
                service.assess(List.of("탄산칼슘", "산화마그네슘")).warnings()
                        .stream().map(IngredientGuide::code).toList());
    }
}
