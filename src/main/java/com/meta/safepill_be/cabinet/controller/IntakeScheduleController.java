package com.meta.safepill_be.cabinet.controller;

import com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek;
import com.meta.safepill_be.cabinet.dto.IntakeScheduleRequestDto;
import com.meta.safepill_be.cabinet.dto.IntakeScheduleResponseDto;
import com.meta.safepill_be.cabinet.service.IntakeScheduleService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class IntakeScheduleController {
    private final IntakeScheduleService intakeScheduleService;
    private final JwtUtil jwtUtil;

    @PostMapping("/{regId}")
    public ResponseEntity<List<IntakeScheduleResponseDto>> registerSchedule(
            @RequestHeader("Authorization") String token,
            @PathVariable Long regId,
            @RequestBody IntakeScheduleRequestDto requestDto) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeScheduleService.addSchedule(loginId, regId, requestDto));
    }

    @GetMapping("/today")
    public ResponseEntity<List<IntakeScheduleResponseDto>> getTodaySchedules(
            @RequestHeader("Authorization") String token) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeScheduleService.getTodaySchedules(loginId));
    }

    @GetMapping
    public ResponseEntity<List<IntakeScheduleResponseDto>> getSchedulesByDay(
            @RequestHeader("Authorization") String token,
            @RequestParam("day") String day) {
        String loginId = extractLoginId(token);
        ScheduleDayOfWeek dayOfWeek = ScheduleDayOfWeek.valueOf(day.trim().toUpperCase());
        return ResponseEntity.ok(intakeScheduleService.getSchedulesByDay(loginId, dayOfWeek));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<String> deleteSchedule(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scheduleId) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeScheduleService.deleteSchedule(loginId, scheduleId));
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<IntakeScheduleResponseDto> updateSchedule(
            @RequestHeader("Authorization") String token,
            @PathVariable Long scheduleId,
            @RequestBody IntakeScheduleRequestDto requestDto) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(intakeScheduleService.updateSchedule(loginId, scheduleId, requestDto));
    }

    private String extractLoginId(String token) {
        String actualToken = token.replace("Bearer ", "");
        return jwtUtil.getLoginIdFromToken(actualToken);
    }
}
