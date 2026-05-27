package com.meta.safepill_be.user.dto;

import com.meta.safepill_be.medicine.domain.CustomGuideInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthProfileResponseDto {
    private Long id;
    private String disease;
    private String allergy;
    private CustomGuideInfo customGuide;
}
