package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.dto.InteractionAnalyzeRequestDto;
import com.meta.safepill_be.medicine.dto.InteractionAnalyzeResponseDto;
import com.meta.safepill_be.medicine.service.InteractionService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/interactions")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;
    private final JwtUtil jwtUtil;

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

    @GetMapping("/my-cabinet/analyze")
    public ResponseEntity<List<InteractionAnalyzeResponseDto>> analyzeMyCabinet(
            @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        String loginId = jwtUtil.getLoginIdFromToken(actualToken);
        List<InteractionAnalyzeResponseDto> result = interactionService.analyzeMyCabinetInteractions(loginId);
        return ResponseEntity.ok(result);
    }
}
