package com.meta.safepill_be.vision.controller;

import com.meta.safepill_be.common.dto.ErrorResponseDto;
import com.meta.safepill_be.vision.client.VisionAiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class VisionExceptionHandler {
    @ExceptionHandler(VisionAiException.class)
    public ResponseEntity<ErrorResponseDto> handleAiError(VisionAiException error) {
        return ResponseEntity.status(error.getStatus()).body(ErrorResponseDto.builder()
                .code(error.getCode()).message(error.getMessage()).build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleOversizedImage(MaxUploadSizeExceededException error) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ErrorResponseDto.builder()
                .code("IMAGE_TOO_LARGE").message("이미지는 30MB 이하로 선택해주세요.").build());
    }
}
