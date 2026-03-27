package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.dto.MedicineResponseDto;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {
    private final MedicineMasterRepository medicineMasterRepository;

    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.medicine-identify}")
    private String medicineIdentifyEndpoint;

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

    public List<MedicineMaster> getAllMedicines() {
        return medicineMasterRepository.findAll();
    }
}