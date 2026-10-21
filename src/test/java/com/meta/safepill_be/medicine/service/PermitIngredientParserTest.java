package com.meta.safepill_be.medicine.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermitIngredientParserTest {
    // 식약처 허가정보 API의 써스펜데이정 실제 응답
    private static final String MATERIAL = "총량 : 1정679.8밀리그램|성분명 : 아세트아미노펜|분량 : 500|단위 : 밀리그램|규격 : KP|성분정보 : |비고 : ;"
            + "총량 : 1정679.8밀리그램|성분명 : 클로르페니라민말레산염|분량 : 2|단위 : 밀리그램|규격 : KP|성분정보 : |비고 : ;"
            + "총량 : 1정679.8밀리그램|성분명 : 슈도에페드린염산염|분량 : 30|단위 : 밀리그램|규격 : USP|성분정보 : |비고 :";
    private static final String MAIN = "[M040353]아세트아미노펜|[M203364]슈도에페드린염산염|[M223211]클로르페니라민말레산염";

    @Test
    void parsesNameDosageAndUnitFromMaterialName() {
        var parsed = PermitIngredientParser.parse(MATERIAL, MAIN);
        assertEquals(3, parsed.size());
        assertEquals("아세트아미노펜", parsed.get(0).name());
        assertEquals(new BigDecimal("500"), parsed.get(0).dosage());
        assertEquals("밀리그램", parsed.get(0).unit());
        assertEquals(new BigDecimal("30"), parsed.get(2).dosage());
    }

    @Test
    void fallsBackToMainIngredientNamesWithoutCodePrefix() {
        var parsed = PermitIngredientParser.parse(null, MAIN);
        assertEquals(3, parsed.size());
        assertEquals("아세트아미노펜", parsed.get(0).name());
        assertNull(parsed.get(0).dosage());
    }

    @Test
    void unifiesKnownSpellingVariant() {
        var parsed = PermitIngredientParser.parse(null, "[M222865]수도에페드린염산염");
        assertEquals("슈도에페드린염산염", parsed.get(0).name());
    }

    @Test
    void ignoresNonNumericDosageInsteadOfGuessing() {
        var parsed = PermitIngredientParser.parse("성분명 : 가나다|분량 : 1.5~3|단위 : 그램", null);
        assertEquals(1, parsed.size());
        assertNull(parsed.get(0).dosage());
    }

    @Test
    void deduplicatesSameIngredientAndHandlesBlank() {
        assertEquals(1, PermitIngredientParser.parse("성분명 : A|분량 : 1;성분명 : A|분량 : 2", null).size());
        assertTrue(PermitIngredientParser.parse("", " ").isEmpty());
    }

    @Test
    void extractsIngredientFromTrailingParenthesesOfProductName() {
        assertEquals("아세트아미노펜", PermitIngredientParser.ingredientFromProductName("한신타미놀캅셀(아세트아미노펜)"));
        assertNull(PermitIngredientParser.ingredientFromProductName("써스펜데이정"));
    }
}
