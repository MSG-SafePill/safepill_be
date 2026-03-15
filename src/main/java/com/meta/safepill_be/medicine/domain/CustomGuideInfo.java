package com.meta.safepill_be.medicine.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CustomGuideInfo {
    // 1. 🚫 피해야 할 성분 (DUR 기준 및 사용자 기저질환/알레르기 반영)
    // 예: ["카페인", "아스피린", "이부프로펜"]
    private List<String> avoidIngredients;

    // 2. 💊 추천 영양 성분 (사용자 상태 기반 맞춤 추천)
    // 예: ["비타민 D", "오메가3", "마그네슘"]
    private List<String> recommendedIngredients;

    // 3. 🍽️ 식단 및 음식 상호작용 주의사항
    // 예: ["자몽 주스와 함께 복용 금지", "우유 등 유제품 섭취 후 2시간 띄우기"]
    private List<String> dietaryPrecautions;

    // 4. ⚠️ 주의해야 할 부작용 키워드 (사용자가 예민하게 반응했던 증상들)
    // 예: ["심한 졸음", "위장 장애", "발진"]
    private List<String> warningSideEffects;

    // 5. 🤖 AI 종합 코멘트 (챗봇이 요약해주는 한 줄 평)
    // 예: "위장이 약하신 편이니 모든 약은 식후 30분에 복용하시고, 카페인 섭취를 줄이세요."
    private String aiSummaryComment;
}
