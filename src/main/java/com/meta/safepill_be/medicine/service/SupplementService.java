package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.dto.SupplementResponseDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplementService {
    private final SupplementMasterRepository supplementMasterRepository;
    private final IngredientMasterRepository ingredientMasterRepository;

    @Value("${open-api.food-safety.base-url}")
    private String baseUrl;

    @Value("${open-api.food-safety.key}")
    private String apiKey;

    @Value("${open-api.food-safety.service-id}")
    private String serviceId;

    @Transactional
    public void fetchAndSaveSupplements() {
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

        int startIdx = 1;
        int endIdx = 100;

        String endpoint = String.format("/%s/%s/json/%d/%d", apiKey, serviceId, startIdx, endIdx);

        System.out.println("🚀 건강기능식품 API 호출 주소: " + baseUrl + endpoint);

        try {
            SupplementResponseDto response = webClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(SupplementResponseDto.class)
                    .block();

            if (response != null && response.getData() != null && response.getData().getItems() != null) {
                List<SupplementResponseDto.Item> items = response.getData().getItems();
                System.out.println("✅ 통신 성공! 가져온 영양제 개수: " + items.size() + "개");

                if (!items.isEmpty()) {
                    System.out.println("👉 [테스트] 1번 영양제 이름: " + items.get(0).getSupplementName());
                    System.out.println("👉 [테스트] 1번 제조사: " + items.get(0).getManufacturer());
                    System.out.println("👉 [테스트] 1번 원재료 텍스트: " + items.get(0).getRawMaterial());
                }
            } else {
                System.out.println("⚠️ API 응답은 왔는데 데이터가 비어있습니다.");
            }

        } catch (Exception e) {
            System.out.println("❌ API 호출 중 에러 발생: " + e.getMessage());
        }
    }
}