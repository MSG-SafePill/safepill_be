package com.meta.safepill_be.cabinet.controller;

import com.meta.safepill_be.cabinet.dto.OcrRegisterRequestDto;
import com.meta.safepill_be.cabinet.dto.OcrRegisterResponseDto;
import com.meta.safepill_be.cabinet.service.OcrRegistrationService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrRegistrationController {
    private final OcrRegistrationService ocrRegistrationService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<OcrRegisterResponseDto> register(
            @RequestHeader("Authorization") String token,
            @RequestBody OcrRegisterRequestDto requestDto) {
        String loginId = jwtUtil.getLoginIdFromToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(ocrRegistrationService.register(loginId, requestDto));
    }
}
