package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.service.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}