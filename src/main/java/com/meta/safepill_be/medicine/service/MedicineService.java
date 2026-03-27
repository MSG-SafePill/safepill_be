package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.dto.IngredientResponseDto;
import com.meta.safepill_be.medicine.dto.MedicineResponseDto;
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
                    MedicineMaster medicine = MedicineMaster.builder()
                            .itemSeq(item.getItemSeq())
                            .medicineName(item.getMedicineName())
                            .medicineManufacturer(item.getMedicineManufacturer())
                            .ingredients("")
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

                        medicine.getIngredient().add(medicineIngredient);
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

    public List<MedicineMaster> getAllMedicines() {
        return medicineMasterRepository.findAll();
    }
}