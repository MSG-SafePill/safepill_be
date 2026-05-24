package com.meta.safepill_be.common.exception;

import com.meta.safepill_be.common.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(error("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error("FORBIDDEN", e.getMessage()));
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponseDto> handleInvalidRequest(Exception e) {
        return ResponseEntity.badRequest().body(error("INVALID_REQUEST", e.getMessage()));
    }

    private ErrorResponseDto error(String code, String message) {
        return ErrorResponseDto.builder()
                .code(code)
                .message(message)
                .build();
    }
}
