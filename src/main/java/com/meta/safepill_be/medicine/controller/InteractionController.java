package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/interactions")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncInteractionRules() {
        interactionService.fetchAndSaveInteractionRules();
        return ResponseEntity.ok("🔥 상극 데이터 파이프라인 가동 완료! DB를 확인해주세요.");
    }
}