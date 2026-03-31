package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.AppearanceInfo;
import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.dto.IngredientResponseDto;
import com.meta.safepill_be.medicine.dto.MedicineResponseDto;
import com.meta.safepill_be.medicine.dto.PrecautionResponseDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineMasterRepository medicineMasterRepository;
    private final IngredientMasterRepository ingredientMasterRepository;

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

    @Transactional
    public void fetchMedicineDataFromApi() {
        WebClient webClient = createWebClient();
        System.out.println("🚀 데이터 파싱 및 DB 저장을 시작합니다...");
        try {
            MedicineResponseDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(medicineIdentifyEndpoint)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", "1")
                            .queryParam("numOfRows", "10") // 10개만 테스트
                            .queryParam("type", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(MedicineResponseDto.class)
                    .block();
            if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
                List<MedicineResponseDto.Item> items = response.getBody().getItems();
                for (MedicineResponseDto.Item item : items) {
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
                        System.out.println("✅ DB 저장 완료: " + medicine.getMedicineName());
                    } else {
                        System.out.println("⚠️ 이미 존재하는 약품입니다: " + item.getMedicineName());
                    }
                }
            }
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
        return WebClient.builder().uriBuilderFactory(factory).build();
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
        cleaned = cleaned.replaceAll("<[^>]+>", "");
        cleaned = cleaned.replace("&#x2981;", "• ");
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        cleaned = cleaned.replace("\r", "");
        cleaned = cleaned.replaceAll("\\n\\s+", "\n");
        cleaned = cleaned.replace("[]\n", "").replace("[]", "");
        String result = cleaned.trim();
        return result.isEmpty() ? "정보 없음" : result;
    }

    public List<MedicineMaster> getAllMedicines() {
        return medicineMasterRepository.findAll();
    }
}