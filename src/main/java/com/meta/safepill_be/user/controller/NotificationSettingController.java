package com.meta.safepill_be.user.controller;

import com.meta.safepill_be.user.dto.NotificationSettingRequestDto;
import com.meta.safepill_be.user.dto.NotificationSettingResponseDto;
import com.meta.safepill_be.user.service.NotificationSettingService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {
    private final NotificationSettingService notificationSettingService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<NotificationSettingResponseDto> getSettings(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(notificationSettingService.getSettings(extractLoginId(token)));
    }

    @PutMapping
    public ResponseEntity<NotificationSettingResponseDto> upsertSettings(
            @RequestHeader("Authorization") String token,
            @RequestBody NotificationSettingRequestDto requestDto) {
        return ResponseEntity.ok(notificationSettingService.upsertSettings(extractLoginId(token), requestDto));
    }

    private String extractLoginId(String token) {
        return jwtUtil.getLoginIdFromToken(token.replace("Bearer ", ""));
    }
}
