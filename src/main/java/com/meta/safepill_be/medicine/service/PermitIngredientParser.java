package com.meta.safepill_be.medicine.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 식약처 의약품 허가정보(getDrugPrdtPrmsnDtlInq06)의 주성분 문자열을 파싱한다.
 * 고장난 성분 상세 API 대신 이 응답의 MATERIAL_NAME / MAIN_ITEM_INGR를 사용한다.
 */
public final class PermitIngredientParser {
    public record Parsed(String name, BigDecimal dosage, String unit) {}

    private static final Pattern CODE_PREFIX = Pattern.compile("^\\[[^\\]]*\\]");
    private static final Pattern NAME_IN_PARENTHESES = Pattern.compile("\\(([^()]+)\\)\\s*$");
    private static final Map<String, String> ALIASES = Map.of(
            "수도에페드린염산염", "슈도에페드린염산염"
    );

    private PermitIngredientParser() {}

    /** MATERIAL_NAME을 우선 쓰고(함량·단위 포함), 없으면 MAIN_ITEM_INGR(이름만)로 대체한다. */
    public static List<Parsed> parse(String materialName, String mainItemIngr) {
        List<Parsed> fromMaterial = parseMaterialName(materialName);
        return fromMaterial.isEmpty() ? parseMainItemIngr(mainItemIngr) : fromMaterial;
    }

    static List<Parsed> parseMaterialName(String raw) {
        Map<String, Parsed> byName = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        for (String block : raw.split(";")) {
            String name = null;
            BigDecimal dosage = null;
            String unit = null;
            for (String field : block.split("\\|")) {
                int colon = field.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String key = field.substring(0, colon).trim();
                String value = field.substring(colon + 1).trim();
                switch (key) {
                    case "성분명" -> name = normalizeName(value);
                    case "분량" -> dosage = parseDosage(value);
                    case "단위" -> unit = value.isEmpty() ? null : value;
                    default -> { }
                }
            }
            if (name != null && !byName.containsKey(name)) {
                byName.put(name, new Parsed(name, dosage, unit));
            }
        }
        return new ArrayList<>(byName.values());
    }

    static List<Parsed> parseMainItemIngr(String raw) {
        Map<String, Parsed> byName = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        for (String token : raw.split("\\|")) {
            String name = normalizeName(CODE_PREFIX.matcher(token.trim()).replaceFirst(""));
            if (name != null && !byName.containsKey(name)) {
                byName.put(name, new Parsed(name, null, null));
            }
        }
        return new ArrayList<>(byName.values());
    }

    /** API에 허가정보가 없는 약의 마지막 보완책: 제품명 끝 괄호 안의 성분명 (예: 한신타미놀캅셀(아세트아미노펜)). */
    public static String ingredientFromProductName(String productName) {
        if (productName == null) {
            return null;
        }
        Matcher matcher = NAME_IN_PARENTHESES.matcher(productName.trim());
        return matcher.find() ? normalizeName(matcher.group(1)) : null;
    }

    static String normalizeName(String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.trim();
        if (name.isEmpty() || name.equals("정보 없음")) {
            return null;
        }
        return ALIASES.getOrDefault(name, name);
    }

    private static BigDecimal parseDosage(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
