package com.meta.safepill_be.common.exception;

import com.meta.safepill_be.common.dto.ErrorResponseDto;
import io.jsonwebtoken.JwtException;
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

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("UNAUTHORIZED", "로그인이 만료되었습니다. 다시 로그인해주세요."));
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
