package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.dto.InteractionAnalyzeRequestDto;
import com.meta.safepill_be.medicine.dto.InteractionAnalyzeResponseDto;
import com.meta.safepill_be.medicine.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/analyze")
    public ResponseEntity<List<InteractionAnalyzeResponseDto>> analyzeInteractions(
            @RequestBody InteractionAnalyzeRequestDto requestDto) {
        System.out.println("🚨 [컨트롤러 진입 성공] 프론트엔드 요청이 무사히 도착했습니다! 약 ID: " + requestDto.getMedicineIds());
        List<InteractionAnalyzeResponseDto> result = interactionService.analyzeInteractions(requestDto.getMedicineIds());
        return ResponseEntity.ok(result);
    }
}