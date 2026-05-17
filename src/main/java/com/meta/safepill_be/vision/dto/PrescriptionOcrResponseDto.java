package com.meta.safepill_be.vision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PrescriptionOcrResponseDto {
    private String requestId;
    private String status;
    private List<PrescriptionOcrItemResponseDto> items;
}
