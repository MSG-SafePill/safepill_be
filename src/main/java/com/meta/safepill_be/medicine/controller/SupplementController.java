package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.service.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
public class SupplementController {
    private final SupplementService supplementService;
    @PostMapping("/sync")
    public ResponseEntity<String> syncSupplements() {
        supplementService.fetchAndSaveSupplements();
        return ResponseEntity.ok("✅ 영양제 데이터 수집 API 호출 완료!");
    }

    @PostMapping("/sync-limits")
    public ResponseEntity<String> syncIngredientLimits() {
        supplementService.fetchAndUpdateIngredientLimits();
        return ResponseEntity.ok("✅ 상한량 데이터 수집 완료!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplementMaster> getSupplementDetail(@PathVariable Long id) {
        SupplementMaster supplement = supplementService.getSupplementDetail(id);
        return ResponseEntity.ok(supplement);
    }
}