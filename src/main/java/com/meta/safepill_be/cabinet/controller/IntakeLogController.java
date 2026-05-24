package com.meta.safepill_be.cabinet.controller;

import com.meta.safepill_be.cabinet.dto.IntakeLogRequestDto;
import com.meta.safepill_be.cabinet.dto.IntakeLogResponseDto;
import com.meta.safepill_be.cabinet.service.IntakeLogService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/intake-logs")
@RequiredArgsConstructor
public class IntakeLogController {
    private final IntakeLogService intakeLogService;
    private final JwtUtil jwtUtil;

    @PostMapping("/{scheduleId}")
    public ResponseEntity<IntakeLogResponseDto> createLog(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scheduleId,
            @RequestBody IntakeLogRequestDto requestDto) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeLogService.createLog(loginId, scheduleId, requestDto));
    }

    @PatchMapping("/{logId}")
    public ResponseEntity<IntakeLogResponseDto> updateLog(
            @RequestHeader("Authorization") String token,
            @PathVariable Long logId,
            @RequestBody IntakeLogRequestDto requestDto) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeLogService.updateLog(loginId, logId, requestDto));
    }

    @GetMapping
    public ResponseEntity<List<IntakeLogResponseDto>> getLogsByDate(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeLogService.getLogsByDate(loginId, date));
    }

    private String extractLoginId(String token) {
        String actualToken = token.replace("Bearer ", "");
        return jwtUtil.getLoginIdFromToken(actualToken);
    }
}
