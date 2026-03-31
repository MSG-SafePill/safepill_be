package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {
    private final MedicineService medicineService;
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
}