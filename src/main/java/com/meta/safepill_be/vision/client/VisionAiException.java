package com.meta.safepill_be.vision.client;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VisionAiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public VisionAiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
