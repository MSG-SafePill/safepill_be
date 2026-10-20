package com.meta.safepill_be.medicine.guide;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

/**
 * Versioned educational catalog, independent of mutable database IDs.
 * Match only explicit aliases; never infer an ingredient from product names,
 * substrings, dosage text, or an LLM response.
 */
@Service
public class IngredientGuideService {
    private final Map<String, IngredientGuide> aliases = new HashMap<>();

    public IngredientGuideService() {
        register(new IngredientGuide("CALCIUM", "칼슘", "EXCESS_INTAKE",
                        "과다섭취 주의", "여러 제품의 칼슘 함량을 확인하세요.",
                        "칼슘 보충제는 변비나 복부 불편을 일으킬 수 있습니다. 식품과 보충제의 총 섭취량을 함께 확인하세요. "
                                + "갑상선호르몬제나 일부 항생제의 흡수에 영향을 줄 수 있으므로 함께 복용한다면 의사·약사에게 복용 간격을 확인하세요.",
                        "NIH ODS · Calcium", "https://ods.od.nih.gov/factsheets/Calcium-Consumer/", "2026-10-20"),
                "칼슘", "calcium", "탄산칼슘", "calcium carbonate", "구연산칼슘", "calcium citrate",
                "시트르산칼슘", "젖산칼슘", "calcium lactate", "글루콘산칼슘", "calcium gluconate");
        register(new IngredientGuide("VITAMIN_C", "비타민 C", "EXCESS_INTAKE",
                        "과다섭취 주의", "고용량·중복 섭취에 주의하세요.",
                        "비타민 C를 과다 섭취하면 설사, 메스꺼움, 복통이 생길 수 있습니다. "
                                + "철 과부하 질환이 있거나 항암 치료 중이라면 고용량 보충제를 사용하기 전에 의료진과 상담하세요.",
                        "NIH ODS · Vitamin C", "https://ods.od.nih.gov/factsheets/VitaminC-Consumer/", "2026-10-20"),
                "비타민C", "비타민씨", "vitamin C", "아스코르브산", "아스코르빈산",
                "ascorbic acid", "L-ascorbic acid", "L-아스코르브산");
        register(new IngredientGuide("ACETAMINOPHEN", "아세트아미노펜", "DUPLICATE_USE",
                        "중복복용·간손상 주의", "감기약·진통제에 같은 성분이 있는지 확인하세요.",
                        "아세트아미노펜 함유 제품을 동시에 사용하면 자신도 모르게 과량 복용할 수 있고 심각한 간손상이 생길 수 있습니다. "
                                + "제품의 용량·복용 간격을 지키고, 간질환이 있거나 음주한다면 의사·약사에게 확인하세요. "
                                + "과량 복용이 의심되면 증상이 없어도 즉시 의료 도움을 받으세요.",
                        "FDA · Acetaminophen",
                        "https://www.fda.gov/drugs/safe-use-over-counter-pain-relievers-and-fever-reducers/acetaminophen",
                        "2026-10-20"),
                "아세트아미노펜", "acetaminophen", "paracetamol", "파라세타몰", "APAP");
        register(new IngredientGuide("MAGNESIUM", "마그네슘", "EXCESS_INTAKE",
                        "과다섭취 주의", "보충제·제산제·변비약의 중복 성분을 확인하세요.",
                        "보충제나 의약품으로 마그네슘을 과다 섭취하면 설사, 메스꺼움, 복통이 생길 수 있습니다. "
                                + "매우 많은 양은 심장박동 이상 등 심각한 문제를 일으킬 수 있습니다. "
                                + "일부 항생제와 골다공증 약의 흡수에 영향을 줄 수 있으므로 의사·약사에게 복용 간격을 확인하세요.",
                        "NIH ODS · Magnesium", "https://ods.od.nih.gov/factsheets/Magnesium-Consumer/", "2026-10-20"),
                "마그네슘", "magnesium", "산화마그네슘", "magnesium oxide",
                "수산화마그네슘", "magnesium hydroxide", "구연산마그네슘", "magnesium citrate",
                "시트르산마그네슘");
        register(new IngredientGuide("MORPHINE", "모르핀", "DEPENDENCE",
                        "의존·호흡억제 주의", "처방대로 사용하고 임의로 증량하거나 갑자기 중단하지 마세요.",
                        "모르핀은 의존·습관성 및 심각한 호흡억제 위험이 있습니다. 술이나 일부 진정 작용 약물과 함께 사용하면 위험이 커질 수 있습니다. "
                                + "복용량 변경·중단은 처방 의료진과 상의하고 다른 사람에게 나누어 주지 마세요. "
                                + "호흡이 느려지거나 숨쉬기 어려우면 즉시 응급 의료 도움을 받으세요.",
                        "MedlinePlus · Morphine", "https://medlineplus.gov/druginfo/meds/a682133.html", "2026-10-20"),
                "모르핀", "morphine", "모르핀황산염", "황산모르핀", "모르핀황산염수화물",
                "morphine sulfate", "morphine sulfate hydrate", "모르핀염산염", "염산모르핀",
                "모르핀염산염수화물", "morphine hydrochloride", "morphine hydrochloride hydrate");
    }

    public IngredientGuideAssessment assess(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return new IngredientGuideAssessment("NO_INGREDIENT_DATA", List.of(), List.of());
        }
        Map<String, IngredientGuide> matches = new LinkedHashMap<>();
        Set<String> unmatched = new LinkedHashSet<>();
        boolean hasName = false;
        for (String name : ingredientNames) {
            String normalized = normalize(name);
            hasName |= !normalized.isEmpty();
            IngredientGuide guide = aliases.get(normalized);
            if (guide == null) {
                unmatched.add(normalized.isEmpty() ? "성분명 없음" : name.strip());
            } else {
                matches.putIfAbsent(guide.code(), guide);
            }
        }
        String status = !hasName ? "NO_INGREDIENT_DATA"
                : matches.isEmpty() ? "NO_REGISTERED_GUIDE"
                : unmatched.isEmpty() ? "AVAILABLE" : "PARTIAL";
        return new IngredientGuideAssessment(status, List.copyOf(matches.values()), List.copyOf(unmatched));
    }

    private void register(IngredientGuide guide, String... names) {
        for (String name : names) {
            String key = normalize(name);
            if (aliases.putIfAbsent(key, guide) != null) {
                throw new IllegalStateException("Duplicate ingredient guide alias: " + name);
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Z}]+", "");
    }
}
