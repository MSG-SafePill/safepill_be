package com.meta.safepill_be.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.safepill_be.common.dto.GeminiResponseDto;
import com.meta.safepill_be.medicine.dto.LlmMedicineResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmMedicineResponseDto askMedicineDetails(String medicineName) {
        WebClient webClient = WebClient.create();
        String prompt = "너는 대한민국 식약처 데이터를 바탕으로 복약 지도를 하는 전문 약사야. " +
                "'" + medicineName + "' 이라는 약(또는 성분)의 효능, 용법, 주의사항을 각각 3줄 이내로 요약해서 반드시 JSON 형식으로만 반환해. " +
                "키 이름은 무조건 efficacy, use_method, precautions 로 해줘. " +
                "precautions는 반드시 내용에 맞게 '[주의]' 또는 '[경고]' 라는 머리말로 시작해줘. 다른 군더더기 말은 절대 하지마.";
        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json"));
        try {
            RestTemplate restTemplate = new RestTemplate();
            String fullUrl = geminiApiUrl.trim() + "?key=" + geminiApiKey.trim();
            java.net.URI uri = java.net.URI.create(fullUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<GeminiResponseDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    GeminiResponseDto.class
            );
            if (response.getBody() != null && response.getBody().getCandidates() != null && !response.getBody().getCandidates().isEmpty()) {
                String jsonText = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
                return objectMapper.readValue(jsonText, LlmMedicineResponseDto.class);
            }
        } catch (Exception e) {
            System.err.println("❌ 제미나이 API 호출 실패 (" + medicineName + "): " + e.getMessage());
        }
        return null;
    }
}