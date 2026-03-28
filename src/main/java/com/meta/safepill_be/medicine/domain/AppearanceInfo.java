package com.meta.safepill_be.medicine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppearanceInfo {
    private String shape;       // 모양 (예: 원형, 타원형)
    private String color;       // 색상 (예: 하양, 연두)
    private String formulation; // 성상/제형 (예: 흰색의 원형 필름코팅정)
    private String imageUrl;    // 약 이미지 Url
    private String lineFront;   // 앞면 분할선 (예: +, -)
    private String lineBack;    // 뒷면 분할선
    private String printFront;  // 앞면 식별 마크
    private String printBack;   // 뒷면 식별 마크
}
