package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.AppearanceInfo;
import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.SupplementIngredient;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.IngredientLimitResponseDto;
import com.meta.safepill_be.medicine.dto.SupplementResponseDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
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

    @Value("${open-api.food-safety.limit-service-id}")
    private String limitServiceId;

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
                for (SupplementResponseDto.Item item : items) {
                    if (supplementMasterRepository.findByItemSeq(item.getItemSeq()).isPresent()) {
                        continue;
                    }
                    AppearanceInfo appearance = AppearanceInfo.builder()
                            .formulation(getOrDefault(item.getDispos()))
                            .build();
                    SupplementMaster supplement = SupplementMaster.builder()
                            .itemSeq(item.getItemSeq())
                            .supplementName(getOrDefault(item.getSupplementName()))
                            .supplementManufacturer(getOrDefault(item.getManufacturer()))
                            .efficacy(getOrDefault(item.getEfficacy()))
                            .intakeMethod(getOrDefault(item.getIntakeMethod()))
                            .precautions(getOrDefault(item.getPrecautions()))
                            .appearanceInfo(appearance)
                            .build();
                    String rawMaterial = item.getRawMaterial();
                    if (rawMaterial != null && !rawMaterial.isEmpty()) {
                        String[] rawIngredients = rawMaterial.split(",");
                        for (String raw : rawIngredients) {
                            String cleanName = raw.replaceAll("\\([^)]*\\)", "")
                                    .replaceAll("\\[[^\\]]*\\]", "")
                                    .trim();
                            if (cleanName.isEmpty()) continue;
                            IngredientMaster ingredient = ingredientMasterRepository.findByIngredientName(cleanName)
                                    .orElseGet(() -> ingredientMasterRepository.save(
                                            IngredientMaster.builder()
                                                    .ingredientName(cleanName)
                                                    .bestTimeGuide("정보 없음")
                                                    .intakeTip("정보 없음")
                                                    .build()
                                    ));
                            SupplementIngredient supplementIngredient = SupplementIngredient.builder()
                                    .supplementMaster(supplement)
                                    .ingredientMaster(ingredient)
                                    .build();
                            supplement.getIngredients().add(supplementIngredient);
                        }
                    }
                    supplementMasterRepository.save(supplement);
                    System.out.println("✅ 영양제 저장 완료: " + supplement.getSupplementName());
                }
            } else {
                System.out.println("⚠️ API 응답은 왔는데 데이터가 비어있습니다.");
            }
        } catch (Exception e) {
            System.out.println("❌ API 호출 중 에러 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void fetchAndUpdateIngredientLimits() {
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        String endpoint = String.format("/%s/%s/json/1/100", apiKey, limitServiceId);
        System.out.println("🚀 영양소 상한량 API 호출 주소: " + baseUrl + endpoint);
        try {
            IngredientLimitResponseDto response = webClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(IngredientLimitResponseDto.class)
                    .block();
            if (response != null && response.getItems() != null) {
                List<IngredientLimitResponseDto.Item> items = response.getItems();
                System.out.println("✅ 가져온 상한량 데이터 개수: " + items.size() + "개");
                for (IngredientLimitResponseDto.Item item : items) {
                    String cleanName = getOrDefault(item.getIngredientName());
                    if (cleanName.equals("정보 없음")) continue;
                    IngredientMaster ingredient = ingredientMasterRepository.findByIngredientName(cleanName)
                            .orElseGet(() -> IngredientMaster.builder()
                                    .ingredientName(cleanName)
                                    .bestTimeGuide("정보 없음")
                                    .intakeTip("정보 없음")
                                    .build());
                    String limitStr = item.getUpperLimit();
                    BigDecimal finalLimit = null;
                    if (limitStr != null && !limitStr.isEmpty()) {
                        try {
                            if (limitStr.contains("~")) {
                                String[] parts = limitStr.split("~");
                                limitStr = parts[parts.length - 1];
                            }
                            String numericOnly = limitStr.replaceAll("[^0-9.]", "");
                            if (numericOnly.indexOf('.') != numericOnly.lastIndexOf('.')) {
                                int firstDot = numericOnly.indexOf('.');
                                numericOnly = numericOnly.substring(0, firstDot + 1) +
                                        numericOnly.substring(firstDot + 1).replace(".", "");
                            }
                            if (!numericOnly.isEmpty()) {
                                finalLimit = new BigDecimal(numericOnly);
                                BigDecimal maxAllowed = new BigDecimal("99999999");
                                if (finalLimit.compareTo(maxAllowed) > 0) {
                                    finalLimit = maxAllowed;
                                }
                                ingredient.updateLimitAndUnit(
                                        finalLimit,
                                        normalizeUnit(item.getUnit())
                                );
                            }
                        } catch (Exception e) {
                            System.out.println("⚠️ 상한량 숫자 변환 실패 (" + cleanName + ") 원래 텍스트: " + limitStr);
                        }
                    }
                    try {
                        ingredientMasterRepository.save(ingredient);
                        System.out.println("✅ 업데이트: " + ingredient.getIngredientName() + " (상한: " + finalLimit + ")");
                    } catch (Exception e) {
                        System.out.println("🚨 [DB 터짐! 범인 발견] 성분명: " + cleanName + " / 넣으려던 숫자: " + finalLimit);
                    }
                }
            } else {
                System.out.println("⚠️ 상한량 API 응답에 데이터가 없습니다.");
            }
        } catch (Exception e) {
            System.out.println("❌ API 호출 에러: " + e.getMessage());
        }
    }

    private String getOrDefault(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "정보 없음";
        }
        return text.trim();
    }

    private String normalizeUnit(String rawUnit) {
        if (rawUnit == null || rawUnit.trim().isEmpty()) {
            return "정보 없음";
        }
        String unit = rawUnit.trim().toLowerCase();
        if (unit.contains("밀리그램") || unit.contains("미리그램") || unit.equals("mg")) {
            return "mg";
        }
        if (unit.equals("그램") || unit.equals("g")) {
            return "g";
        }
        if (unit.contains("마이크로그램") || unit.contains("마이크로크램") || unit.contains("mcg") || unit.contains("ug") || unit.contains("µg")) {
            return "mcg";
        }
        if (unit.contains("밀리리터") || unit.contains("미리리터") || unit.equals("ml")) {
            return "ml";
        }
        if (unit.equals("리터") || unit.equals("l")) {
            return "L";
        }
        if (unit.contains("국제단위") || unit.contains("아이유") || unit.contains("iu")) {
            return "IU";
        }
        return rawUnit.trim();
    }

    @Transactional(readOnly = true)
    public SupplementMaster getSupplementDetail(Long id) {
        return supplementMasterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 영양제를 찾을 수 없습니다. ID: " + id));
    }
}