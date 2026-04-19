package com.meta.safepill_be.cabinet.controller;

import com.meta.safepill_be.cabinet.dto.MedicationRegRequestDto;
import com.meta.safepill_be.cabinet.service.UserMedicationService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypills")
@RequiredArgsConstructor
public class UserMedicationController {
    private final UserMedicationService userMedicationService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<String> registerMedication(
            @RequestHeader("Authorization") String token,
            @RequestBody MedicationRegRequestDto requestDto) {

        String actualToken = token.replace("Bearer ", "");
        String loginId = jwtUtil.getLoginIdFromToken(actualToken);
        String result = userMedicationService.addMedication(loginId, requestDto);
        return ResponseEntity.ok(result);
    }
}