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
    private String  medicinePrecautionEndpoint;

    @Transactional
    public void fetchMedicineDataFromApi() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .build();

        System.out.println("데이터 파싱 및 DB 저장을 시작합니다...");

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

        if (response != null && response.getBody() != null) {
            List<MedicineResponseDto.Item> items = response.getBody().getItems();
            for (MedicineResponseDto.Item item : items) {
                if (medicineMasterRepository.findByItemSeq(item.getItemSeq()).isEmpty()) {
                    AppearanceInfo appearance = AppearanceInfo.builder()
                            .shape(item.getShape())
                            .color(item.getColor())
                            .formulation(item.getFormulation())
                            .imageUrl(item.getImageUrl())
                            .lineFront(item.getLineFront())
                            .lineBack(item.getLineBack())
                            .printFront(item.getPrintFront())
                            .printBack(item.getPrintBack())
                            .build();

                    MedicineMaster medicine = MedicineMaster.builder()
                            .itemSeq(item.getItemSeq())
                            .medicineName(item.getMedicineName())
                            .medicineManufacturer(item.getMedicineManufacturer())
                            .appearanceInfo(appearance)
                            .precautions("")
                            .build();

                    medicineMasterRepository.save(medicine);
                    System.out.println("✅ DB 저장 완료: " + item.getMedicineName());
                } else {
                    System.out.println("⚠️ 이미 존재하는 약품입니다: " + item.getMedicineName());
                }
            }
        }
    }

    @Transactional
    public void fetchAndSaveIngredients() {
        List<MedicineMaster> medicines = medicineMasterRepository.findAll();

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        WebClient webClient = WebClient.builder().uriBuilderFactory(factory).build();

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
                        String ingName = ingItem.getIngredientName();

                        if (ingName == null || ingName.trim().isEmpty()) {
                            continue;
                        }
                        String dosageStr = ingItem.getDosage();
                        String unitStr = ingItem.getUnit();

                        IngredientMaster ingredientMaster = ingredientMasterRepository.findByIngredientName(ingName)
                                .orElseGet(() -> {
                                    IngredientMaster newIngredient = IngredientMaster.builder()
                                            .ingredientName(ingName)
                                            .unit(unitStr)
                                            .bestTimeGuide("")
                                            .build();
                                    return ingredientMasterRepository.save(newIngredient);
                                });

                        BigDecimal parsedDosage = null;
                        if (dosageStr != null && !dosageStr.trim().isEmpty()) {
                            try {
                                parsedDosage = new BigDecimal(dosageStr.trim());
                            } catch (NumberFormatException e) {
                                System.out.println("⚠️ 용량 숫자 변환 실패 (문자 포함) - 성분명: " + ingName + ", 값: " + dosageStr);
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
                } else {
                    System.out.println("⚠️ [" + medicine.getMedicineName() + "] 성분 정보가 없습니다.");
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

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        WebClient webClient = WebClient.builder().uriBuilderFactory(factory).build();

        System.out.println("🚀 주의사항(Precautions) 데이터 업데이트를 시작합니다...");

        for (MedicineMaster medicine : medicines) {
            try {
                PrecautionResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(medicinePrecautionEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("pageNo", "1")
                                .queryParam("numOfRows", "3") // 주의사항은 보통 1개면 충분하므로 작게 잡습니다.
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
                        String rawEfficacy = precautionItem.getEfficacy();
                        String rawUseMethod = precautionItem.getUseMethod();
                        String rawPrecautions = precautionItem.getPrecautions();

                        if ((rawEfficacy != null && !rawEfficacy.trim().isEmpty()) ||
                                (rawUseMethod != null && !rawUseMethod.trim().isEmpty()) ||
                                (rawPrecautions != null && !rawPrecautions.trim().isEmpty())) {

                            String cleanedEfficacy = cleanXmlText(rawEfficacy);
                            String cleanedUseMethod = cleanXmlText(rawUseMethod);
                            String cleanedPrecautions = cleanXmlText(rawPrecautions);

                            medicine.updateDetails(cleanedEfficacy, cleanedUseMethod, cleanedPrecautions);
                            medicineMasterRepository.save(medicine);

                            System.out.println("✅ [" + medicine.getMedicineName() + "] 상세정보(효능/용법/주의사항) 업데이트 완료!");
                        } else {
                            System.out.println("⚠️ [" + medicine.getMedicineName() + "] 상세정보 텍스트가 모두 비어있습니다.");
                        }
                    }
                } else {
                    System.out.println("⚠️ [" + medicine.getMedicineName() + "] API 응답에 데이터가 없습니다.");
                }
                Thread.sleep(500);

            } catch (Exception e) {
                System.out.println("❌ 에러 발생 (" + medicine.getMedicineName() + "): " + e.getMessage());
            }
        }
        System.out.println("🎉 모든 주의사항 데이터 동기화 완료!");
    }

    private String cleanXmlText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
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

        return cleaned.trim();
    }

    public List<MedicineMaster> getAllMedicines() {
        return medicineMasterRepository.findAll();
    }
}