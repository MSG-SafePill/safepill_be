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
    // ⭐️ DB 저장을 위해 아까 만들어둔 리포지토리를 불러옵니다!
    private final MedicineMasterRepository medicineMasterRepository;

    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.medicine-identify}")
    private String medicineIdentifyEndpoint;

    @Transactional // ⭐️ DB에 데이터를 저장할 때는 이 어노테이션이 필수입니다.
    public void fetchMedicineDataFromApi() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .build();

        System.out.println("데이터 파싱 및 DB 저장을 시작합니다...");

        // 1. API 찔러서 DTO(자바 객체)로 똑똑하게 받아오기!
        MedicineResponseDto response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(medicineIdentifyEndpoint)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", "1")
                        .queryParam("numOfRows", "10") // 10개만 테스트
                        .queryParam("type", "json")
                        .build())
                .retrieve()
                // ⭐️ String.class 대신 방금 만든 DTO 클래스를 넣어주면 스프링이 알아서 변환해 줍니다!
                .bodyToMono(MedicineResponseDto.class)
                .block();

        // 2. 알맹이(items)만 쏙 빼기
        if (response != null && response.getBody() != null) {
            List<MedicineResponseDto.Item> items = response.getBody().getItems();

            // 3. 빼온 알맹이들을 하나씩 돌면서 MedicineMaster 엔티티로 만들고 DB에 저장!
            for (MedicineResponseDto.Item item : items) {

                if (medicineMasterRepository.findByItemSeq(item.getItemSeq()).isEmpty()) {

                    // ⭐️ 바로 이 부분이 빌더 패턴의 마법입니다! 어떤 값에 뭐가 들어가는지 너무 명확하죠?
                    MedicineMaster medicine = MedicineMaster.builder()
                            .itemSeq(item.getItemSeq())
                            .medicineName(item.getMedicineName())
                            .medicineManufacturer(item.getMedicineManufacturer())
                            // 💡 nullable = false 속성 에러 방지를 위해 임시 값 세팅
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
}