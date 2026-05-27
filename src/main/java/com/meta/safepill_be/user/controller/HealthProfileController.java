package com.meta.safepill_be.user.controller;

import com.meta.safepill_be.user.dto.HealthProfileRequestDto;
import com.meta.safepill_be.user.dto.HealthProfileResponseDto;
import com.meta.safepill_be.user.service.HealthProfileService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/health")
@RequiredArgsConstructor
public class HealthProfileController {
    private final HealthProfileService healthProfileService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<HealthProfileResponseDto> getHealthProfile(
            @RequestHeader("Authorization") String token) {
        String loginId = extractLoginId(token);
        HealthProfileResponseDto responseDto = healthProfileService.getHealthProfile(loginId);
        return responseDto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(responseDto);
    }

    @PutMapping
    public ResponseEntity<HealthProfileResponseDto> upsertHealthProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody HealthProfileRequestDto requestDto) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(healthProfileService.upsertHealthProfile(loginId, requestDto));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteHealthProfile(
            @RequestHeader("Authorization") String token) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(healthProfileService.deleteHealthProfile(loginId));
    }

    private String extractLoginId(String token) {
        String actualToken = token.replace("Bearer ", "");
        return jwtUtil.getLoginIdFromToken(actualToken);
    }
}
