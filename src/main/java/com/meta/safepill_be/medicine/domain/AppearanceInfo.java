package com.meta.safepill_be.medicine.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppearanceInfo {
    private String shape;      // 모양 (예: 원형, 타원형)
    private String color;      // 색상 (예: 하양, 연두)
    private String lineFront;  // 앞면 분할선 (예: +, -)
    private String lineBack;   // 뒷면 분할선
    private String mark;       // 식별 마크
}
