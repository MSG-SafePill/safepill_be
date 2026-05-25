package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.dto.MedicationMatchRequestDto;
import com.meta.safepill_be.medicine.dto.MedicationMatchResultDto;
import com.meta.safepill_be.medicine.service.MedicationMatchService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medication-matches")
@RequiredArgsConstructor
public class MedicationMatchController {
    private final MedicationMatchService medicationMatchService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<List<MedicationMatchResultDto>> match(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody MedicationMatchRequestDto requestDto) {
        String loginId = token == null || token.isBlank()
                ? null
                : jwtUtil.getLoginIdFromToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(medicationMatchService.match(loginId, requestDto));
    }
}
