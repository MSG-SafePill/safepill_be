package com.meta.safepill_be.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto {
    private String code;
    private String message;
}
