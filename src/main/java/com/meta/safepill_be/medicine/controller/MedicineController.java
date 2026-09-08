package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.dto.MedicineAlternativeDto;
import com.meta.safepill_be.medicine.service.MedicineAlternativeService;
import com.meta.safepill_be.medicine.service.MedicineService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {
    private final MedicineService medicineService;
    private final MedicineAlternativeService medicineAlternativeService;
    private final JwtUtil jwtUtil;
    @PostMapping("/sync")
    public ResponseEntity<String> syncMedicineData() {
        medicineService.fetchMedicineDataFromApi();
        return ResponseEntity.ok("✅ 의약품 공공데이터 10개 동기화 완료!");
    }

    @PostMapping("/sync-ingredients")
    public ResponseEntity<String> syncIngredients() {
        medicineService.fetchAndSaveIngredients();
        return ResponseEntity.ok("✅ 의약품 성분 데이터 동기화 완료!");
    }

    @PostMapping("/sync-precautions")
    public ResponseEntity<String> syncPrecautions() {
        medicineService.fetchAndUpdatePrecautions();
        return ResponseEntity.ok("✅ 의약품 주의사항 데이터 동기화 완료!");
    }

    @PostMapping("/sync-details")
    public ResponseEntity<String> syncMedicineDetails() {
        medicineService.syncDrugInfoDetails();
        return ResponseEntity.ok("e약은요 상세 정보 동기화가 백그라운드에서 시작되었습니다.");
    }

    @GetMapping
    public ResponseEntity<List<MedicineMaster>> getAllMedicines() {
        List<MedicineMaster> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineMaster> getMedicineDetail(@PathVariable Long id) {
        MedicineMaster medicine = medicineService.getMedicineDetail(id);
        return ResponseEntity.ok(medicine);
    }

    @GetMapping("/{id}/alternatives")
    public ResponseEntity<List<MedicineAlternativeDto>> getAlternatives(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        String loginId = extractLoginId(token);
        return ResponseEntity.ok(medicineAlternativeService.findAlternatives(id, loginId));
    }

    private String extractLoginId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return jwtUtil.getLoginIdFromToken(token.replace("Bearer ", ""));
        } catch (RuntimeException e) {
            return null;
        }
    }
}