package com.meta.safepill_be.medicine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
@RequiredArgsConstructor
public class MedicineService {
    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.medicine-identify}")
    private String medicineIdentifyEndpoint;

    public void fetchMedicineDataFromApi() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .build();

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(medicineIdentifyEndpoint)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", "1")
                        .queryParam("numOfRows", "10")
                        .queryParam("type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("🎉 API 응답 성공!");
        System.out.println(response);
    }
}