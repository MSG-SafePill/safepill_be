package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.InteractionRule;
import com.meta.safepill_be.medicine.domain.RiskLevel;
import com.meta.safepill_be.medicine.dto.DurResponseDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.InteractionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final InteractionRuleRepository interactionRuleRepository;
    private final IngredientMasterRepository ingredientMasterRepository;

    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.dur-taboo}")
    private String durTabooEndpoint;

    @Transactional
    public void fetchAndSaveInteractionRules() {
        WebClient webClient = createWebClient();
        System.out.println("🚀 병용금기(상극) 데이터 파싱 및 DB 저장을 시작합니다...");
        try {
            DurResponseDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(durTabooEndpoint)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("type", "json")
                            .queryParam("numOfRows", "50")
                            .build())
                    .retrieve()
                    .bodyToMono(DurResponseDto.class)
                    .block();
            if (response == null || response.getBody() == null) {
                System.out.println("❌ API 응답이 없거나 공공데이터포털 서버 에러(XML 반환 등)입니다.");
                return;
            }
            if (response.getBody().getItems() != null) {
                List<DurResponseDto.ItemWrapper> wrappers = response.getBody().getItems();
                int successCount = 0;
                for (DurResponseDto.ItemWrapper wrapper : wrappers) {
                    DurResponseDto.Item item = wrapper.getItem();
                    if (item == null) continue;
                    String nameA = item.getIngredientA();
                    String nameB = item.getIngredientB();
                    String desc = item.getProhibitContent();
                    if (nameA == null || nameB == null || nameA.isEmpty() || nameB.isEmpty()) {
                        continue;
                    }
                    IngredientMaster ingredientA = findOrCreateIngredient(nameA);
                    IngredientMaster ingredientB = findOrCreateIngredient(nameB);
                    InteractionRule rule = new InteractionRule();
                    rule.setIngredientA(ingredientA);
                    rule.setIngredientB(ingredientB);
                    rule.setRiskLevel(RiskLevel.DANGER);
                    rule.setDescription(desc != null ? desc : "병용금기 성분입니다.");
                    interactionRuleRepository.save(rule);
                    successCount++;
                    System.out.println("🔥 저장 완료: " + nameA + " + " + nameB);
                }
                System.out.println("🎉 상극 데이터 총 " + successCount + "건 동기화 완료!");
            } else {
                System.out.println("⚠️ 데이터는 정상 호출되었으나 items 목록이 비어있습니다.");
            }
        } catch (Exception e) {
            System.err.println("❌ 상극 데이터 수집 중 에러: " + e.getMessage());
        }
    }

    private IngredientMaster findOrCreateIngredient(String name) {
        return ingredientMasterRepository.findByIngredientName(name)
                .orElseGet(() -> ingredientMasterRepository.save(
                        IngredientMaster.builder()
                                .ingredientName(name)
                                .bestTimeGuide("정보 없음")
                                .intakeTip("정보 없음")
                                .build()
                ));
    }

    private WebClient createWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return WebClient.builder().uriBuilderFactory(factory).build();
    }
}