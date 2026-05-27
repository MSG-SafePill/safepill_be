package com.meta.safepill_be.user.dto;

import com.meta.safepill_be.medicine.domain.CustomGuideInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HealthProfileRequestDto {
    private String disease;
    private String allergy;
    private CustomGuideInfo customGuide;
}
