package com.meta.safepill_be.cabinet.controller;

import com.meta.safepill_be.cabinet.dto.CabinetItemResponseDto;
import com.meta.safepill_be.cabinet.dto.MedicationRegRequestDto;
import com.meta.safepill_be.cabinet.dto.MedicationPreviewDto;
import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.service.UserMedicationService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<CabinetItemResponseDto>> getMyMedications(
            @RequestHeader("Authorization") String token) {

        String actualToken = token.replace("Bearer ", "");
        String loginId = jwtUtil.getLoginIdFromToken(actualToken);

        List<CabinetItemResponseDto> result = userMedicationService.getMyMedications(loginId);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{regId}")
    public ResponseEntity<String> deleteMedication(
            @RequestHeader("Authorization") String token,
            @PathVariable Long regId) {
        String actualToken = token.replace("Bearer ", "");
        String loginId = jwtUtil.getLoginIdFromToken(actualToken);
        String result = userMedicationService.deleteMedication(loginId, regId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/preview")
    public ResponseEntity<?> previewMedication(@RequestHeader("Authorization") String token,
            @RequestParam String type, @RequestParam Long itemId) {
        String loginId = jwtUtil.getLoginIdFromToken(token.replace("Bearer ", ""));
        final ItemType itemType;
        try {
            itemType = ItemType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "올바른 제품 분류를 선택해주세요."));
        }
        try {
            return ResponseEntity.ok(userMedicationService.previewMedication(loginId, itemType, itemId));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Return directly so /error security dispatch cannot mask a 400/404 as 403.
            return ResponseEntity.status(e.getStatusCode()).body(java.util.Map.of("message", e.getReason()));
        }
    }
}
