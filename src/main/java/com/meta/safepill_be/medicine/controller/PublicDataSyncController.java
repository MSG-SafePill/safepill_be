package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.service.PublicDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public-data")
@RequiredArgsConstructor
public class PublicDataSyncController {
    private final PublicDataSyncService publicDataSyncService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncAllPublicData() {
        publicDataSyncService.syncAll();
        return ResponseEntity.ok("공공데이터 전체 동기화가 완료되었습니다.");
    }
}
