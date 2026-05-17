package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.common.service.GeminiService;
import com.meta.safepill_be.medicine.domain.AppearanceInfo;
import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.dto.*;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.MedicineIngredientRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MedicineService {
    private final MedicineMasterRepository medicineMasterRepository;
    private final IngredientMasterRepository ingredientMasterRepository;
    private final MedicineIngredientRepository medicineIngredientRepository;
    private final GeminiService geminiService;
    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.medicine-identify}")
    private String medicineIdentifyEndpoint;

    @Value("${open-api.data-go-kr.endpoint.medicine-ingredient}")
    private String medicineIngredientEndpoint;

    @Value("${open-api.data-go-kr.endpoint.medicine-precaution}")
    private String medicinePrecautionEndpoint;

    @Value("${open-api.data-go-kr.endpoint.druginfo}")
    private String drugInfoEndpoint;

    @Transactional
    public void fetchMedicineDataFromApi() {
        WebClient webClient = createWebClient();
        System.out.println("🚀 데이터 파싱 및 DB 저장을 시작합니다...");
        int page = 1;
        int numOfRows = 500;
        int savedCount = 0;
        try {
            while (true) {
                final int currentPage = page;
                MedicineResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(medicineIdentifyEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("pageNo", String.valueOf(currentPage))
                                .queryParam("numOfRows", String.valueOf(numOfRows))
                                .queryParam("type", "json")
                                .build())
                        .retrieve()
                        .bodyToMono(MedicineResponseDto.class)
                        .block();
                if (response == null || response.getBody() == null || response.getBody().getItems() == null
                        || response.getBody().getItems().isEmpty()) {
                    break;
                }
                List<MedicineResponseDto.Item> items = response.getBody().getItems();
                for (MedicineResponseDto.Item item : items) {
                    if (item.getItemSeq() == null || item.getItemSeq().trim().isEmpty()) {
                        continue;
                    }
                    if (medicineMasterRepository.findByItemSeq(item.getItemSeq()).isEmpty()) {
                        AppearanceInfo appearance = AppearanceInfo.builder()
                                .shape(getOrDefault(item.getShape()))
                                .color(getOrDefault(item.getColor()))
                                .formulation(getOrDefault(item.getFormulation()))
                                .imageUrl(item.getImageUrl())
                                .lineFront(getOrDefault(item.getLineFront()))
                                .lineBack(getOrDefault(item.getLineBack()))
                                .printFront(getOrDefault(item.getPrintFront()))
                                .printBack(getOrDefault(item.getPrintBack()))
                                .build();
                        MedicineMaster medicine = MedicineMaster.builder()
                                .itemSeq(item.getItemSeq())
                                .medicineName(getOrDefault(item.getMedicineName()))
                                .medicineManufacturer(getOrDefault(item.getMedicineManufacturer()))
                                .appearanceInfo(appearance)
                                .precautions("정보 없음")
                                .build();
                        medicineMasterRepository.save(medicine);
                        savedCount++;
                        System.out.println("✅ DB 저장 완료: " + medicine.getMedicineName());
                    } else {
                        System.out.println("⚠️ 이미 존재하는 약품입니다: " + item.getMedicineName());
                    }
                }
                Integer totalCount = response.getBody().getTotalCount();
                if (totalCount != null && currentPage * numOfRows >= totalCount) {
                    break;
                }
                page++;
                Thread.sleep(100);
            }
            System.out.println("🎉 의약품 기본 데이터 동기화 완료! 신규 저장: " + savedCount + "건");
        } catch (Exception e) {
            System.out.println("❌ API 호출 중 에러 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void fetchAndSaveIngredients() {
        List<MedicineMaster> medicines = medicineMasterRepository.findAll();
        WebClient webClient = createWebClient();
        System.out.println("🚀 성분 데이터 파싱 및 연결을 시작합니다...");
        for (MedicineMaster medicine : medicines) {
            Set<Long> linkedIngredientIds = new HashSet<>();
            for (MedicineIngredient ingredient : medicine.getIngredients()) {
                linkedIngredientIds.add(ingredient.getIngredientMaster().getId());
            }
            try {
                IngredientResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(medicineIngredientEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("pageNo", "1")
                                .queryParam("numOfRows", "100")
                                .queryParam("type", "json")
                                .queryParam("item_seq", medicine.getItemSeq())
                                .build())
                        .retrieve()
                        .bodyToMono(IngredientResponseDto.class)
                        .block();
                if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
                    List<IngredientResponseDto.Item> ingredientItems = response.getBody().getItems();
                    for (IngredientResponseDto.Item ingItem : ingredientItems) {
                        String ingName = getOrDefault(ingItem.getIngredientName());
                        if (ingName.equals("정보 없음")) continue;
                        String unitStr = normalizeUnit(ingItem.getUnit());
                        String dosageStr = ingItem.getDosage();
                        IngredientMaster ingredientMaster = ingredientMasterRepository.findByIngredientName(ingName)
                                .orElseGet(() -> ingredientMasterRepository.save(
                                        IngredientMaster.builder()
                                                .ingredientName(ingName)
                                                .unit(unitStr.equals("정보 없음") ? null : unitStr)
                                                .bestTimeGuide("정보 없음")
                                                .intakeTip("정보 없음")
                                                .build()
                                ));
                        if (linkedIngredientIds.contains(ingredientMaster.getId())
                                || medicineIngredientRepository.existsByMedicineMaster_IdAndIngredientMaster_Id(
                                medicine.getId(), ingredientMaster.getId())) {
                            continue;
                        }
                        BigDecimal parsedDosage = null;
                        if (dosageStr != null && !dosageStr.trim().isEmpty()) {
                            try {
                                parsedDosage = new BigDecimal(dosageStr.trim());
                            } catch (NumberFormatException e) {
                                System.out.println("⚠️ 용량 숫자 변환 실패 - 성분명: " + ingName + ", 값: " + dosageStr);
                            }
                        }
                        MedicineIngredient medicineIngredient = MedicineIngredient.builder()
                                .medicineMaster(medicine)
                                .ingredientMaster(ingredientMaster)
                                .dosage(parsedDosage)
                                .build();
                        medicine.getIngredients().add(medicineIngredient);
                        linkedIngredientIds.add(ingredientMaster.getId());
                    }
                    medicineMasterRepository.save(medicine);
                    System.out.println("✅ [" + medicine.getMedicineName() + "] 성분 연결 완료!");
                }
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("❌ 에러 발생 (" + medicine.getMedicineName() + "): " + e.getMessage());
            }
        }
        System.out.println("🎉 모든 성분 동기화 완료!");
    }

    @Transactional
    public void fetchAndUpdatePrecautions() {
        List<MedicineMaster> medicines = medicineMasterRepository.findAll();
        WebClient webClient = createWebClient();
        System.out.println("🚀 주의사항(Precautions) 데이터 업데이트를 시작합니다...");
        for (MedicineMaster medicine : medicines) {
            try {
                PrecautionResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(medicinePrecautionEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("pageNo", "1")
                                .queryParam("numOfRows", "3")
                                .queryParam("type", "json")
                                .queryParam("item_seq", medicine.getItemSeq())
                                .build())
                        .retrieve()
                        .bodyToMono(PrecautionResponseDto.class)
                        .block();
                if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
                    List<PrecautionResponseDto.Item> items = response.getBody().getItems();
                    if (!items.isEmpty()) {
                        PrecautionResponseDto.Item precautionItem = items.get(0);
                        String cleanedEfficacy = cleanXmlText(precautionItem.getEfficacy());
                        String cleanedUseMethod = cleanXmlText(precautionItem.getUseMethod());
                        String cleanedPrecautions = cleanXmlText(precautionItem.getPrecautions());
                        medicine.updateDetails(cleanedEfficacy, cleanedUseMethod, cleanedPrecautions);
                        medicineMasterRepository.save(medicine);
                        System.out.println("✅ [" + medicine.getMedicineName() + "] 상세정보 업데이트 완료!");
                    }
                }
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("❌ 에러 발생 (" + medicine.getMedicineName() + "): " + e.getMessage());
            }
        }
        System.out.println("🎉 모든 주의사항 데이터 동기화 완료!");
    }

    private WebClient createWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        org.springframework.web.reactive.function.client.ExchangeStrategies strategies =
                org.springframework.web.reactive.function.client.ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                        .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .uriBuilderFactory(factory)
                .build();
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

    private String cleanXmlText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "정보 없음";
        }
        String cleaned = rawText;
        cleaned = cleaned.replace("<![CDATA[", "").replace("]]>", "");
        cleaned = cleaned.replaceAll("<ARTICLE title=\"([^\"]*)\">", "\n\n[$1]\n");
        cleaned = cleaned.replaceAll("(?i)<br\\s*/?>", "\n");
        cleaned = cleaned.replaceAll("(?i)</p>", "\n");
        cleaned = cleaned.replaceAll("<[^>]+>", "");
        cleaned = cleaned.replace("NBSP", " ");
        cleaned = cleaned.replace("&#x2981;", "• ");
        cleaned = cleaned.replace("&lt;", "<");
        cleaned = cleaned.replace("&gt;", ">");
        cleaned = cleaned.replace("&amp;", "&");
        cleaned = cleaned.replace("&quot;", "\"");
        cleaned = cleaned.replace("&apos;", "'");
        cleaned = cleaned.replace("&nbsp;", " ");
        cleaned = cleaned.replace("\r\n", "\n");
        cleaned = cleaned.replace("\r", "");
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        cleaned = cleaned.replace("[]\n", "").replace("[]", "");
        String result = cleaned.trim();
        return result.isEmpty() ? "정보 없음" : result;
    }

    @Transactional(readOnly = true)
    public MedicineMaster getMedicineDetail(Long id) {
        return medicineMasterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 약품을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public void syncDrugInfoDetails() {
        syncDrugInfoDetails(true);
    }

    @Transactional
    public void syncDrugInfoDetails(boolean useLlmFallback) {
        WebClient webClient = createWebClient();
        System.out.println("🚀 'e약은요' 타겟팅 매칭 업데이트를 시작합니다...");
        List<com.meta.safepill_be.medicine.domain.MedicineMaster> targetMedicines = medicineMasterRepository.findByEfficacyIsNull();
        System.out.println("📊 빈칸을 채워야 할 약품 개수: " + targetMedicines.size() + "개");
        int updateCount = 0;
        for (com.meta.safepill_be.medicine.domain.MedicineMaster medicine : targetMedicines) {
            String targetItemSeq = medicine.getItemSeq();
            if (targetItemSeq == null || targetItemSeq.isEmpty()) continue;
            try {
                DrugInfoResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(drugInfoEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("type", "json")
                                .queryParam("itemSeq", targetItemSeq)
                                .build())
                        .retrieve()
                        .bodyToMono(DrugInfoResponseDto.class)
                        .block();
                if (response != null && response.getBody() != null && response.getBody().getItems() != null && !response.getBody().getItems().isEmpty()) {
                    DrugInfoResponseDto.Item item = response.getBody().getItems().get(0);
                    String combinedPrecautions = (item.getAtpnWarnQesitm() != null ? "[경고]\n" + item.getAtpnWarnQesitm() + "\n\n" : "")
                            + (item.getAtpnQesitm() != null ? "[주의]\n" + item.getAtpnQesitm() : "");
                    medicine.updateDetails(
                            cleanXmlText(item.getEfcyQesitm()),
                            cleanXmlText(item.getUseMethodQesitm()),
                            cleanXmlText(combinedPrecautions)
                    );
                    updateCount++;
                    System.out.println("✅ 매칭 성공: " + medicine.getMedicineName());
                } else {
                    if (!useLlmFallback) {
                        System.out.println("⚠️ e약은요 공공데이터에 없음. LLM fallback 없이 건너뜁니다: " + medicine.getMedicineName());
                        continue;
                    }
                    System.out.println("🤖 식약처 DB에 없음. 제미나이에게 " + medicine.getMedicineName() + " 물어보는 중...");
                    LlmMedicineResponseDto llmResponse = null;
                    for (int retry = 1; retry <= 3; retry++) {
                        llmResponse = geminiService.askMedicineDetails(medicine.getMedicineName());
                        if (llmResponse != null) {
                            break;
                        }
                        System.out.println("⚠️ 구글 서버 혼잡(503 등). 5초 후 재시도합니다... (" + retry + "/3)");
                        Thread.sleep(5000);
                    }
                    if (llmResponse != null) {
                        medicine.updateDetails(
                                llmResponse.getEfficacy() != null ? llmResponse.getEfficacy() : "정보 없음",
                                llmResponse.getUse_method() != null ? llmResponse.getUse_method() : "정보 없음",
                                llmResponse.getPrecautions() != null ? llmResponse.getPrecautions() : "특이 주의사항 없음");
                        updateCount++;
                        System.out.println("✅ 제미나이 요약 성공: " + medicine.getMedicineName());
                    } else {
                        System.out.println("❌ 3번 재시도 실패. 이 약은 건너뜁니다: " + medicine.getMedicineName());
                    }
                }
                Thread.sleep(50);
            } catch (Exception e) {
                System.err.println("⚠️ 통신 에러 (" + medicine.getMedicineName() + "): " + e.getMessage());
            }
        }
        System.out.println("🎉 타겟팅 수집 완료! 총 " + updateCount + "개의 약품이 새 생명을 얻었습니다!");
    }

    public List<MedicineMaster> getAllMedicines() {
        return medicineMasterRepository.findAll();
    }
}
