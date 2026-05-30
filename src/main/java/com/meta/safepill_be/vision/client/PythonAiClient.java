package com.meta.safepill_be.vision.client;

import com.meta.safepill_be.chatbot.dto.AiChatRequestDto;
import com.meta.safepill_be.chatbot.dto.AiChatResponseDto;
import com.meta.safepill_be.medicine.dto.AiInteractionAnalyzeRequestDto;
import com.meta.safepill_be.medicine.dto.AiInteractionAnalyzeResponseDto;
import com.meta.safepill_be.vision.dto.AiIdentifyResponseDto;
import com.meta.safepill_be.vision.dto.AiPillClassificationResponseDto;
import com.meta.safepill_be.vision.dto.AiPrescriptionOcrResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PythonAiClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${safepill.ai.base-url}")
    private String aiBaseUrl;

    @Value("${safepill.ai.timeout-ms:5000}")
    private long aiTimeoutMs;

    public AiPillClassificationResponseDto classifyPill(MultipartFile image) {
        return postImage("/classify-pill", image, AiPillClassificationResponseDto.class);
    }

    public AiPrescriptionOcrResponseDto scanPrescription(MultipartFile image) {
        return postImage("/prescription-ocr-upload", image, AiPrescriptionOcrResponseDto.class);
    }

    public AiInteractionAnalyzeResponseDto analyzeInteraction(AiInteractionAnalyzeRequestDto requestDto) {
        return webClientBuilder.build()
                .post()
                .uri(aiBaseUrl + "/interaction/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(AiInteractionAnalyzeResponseDto.class)
                .block(Duration.ofMillis(aiTimeoutMs));
    }

    public AiChatResponseDto chat(AiChatRequestDto requestDto) {
        return webClientBuilder.build()
                .post()
                .uri(aiBaseUrl + "/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(AiChatResponseDto.class)
                .block(Duration.ofMillis(aiTimeoutMs));
    }

    private <T> T postImage(String path, MultipartFile image, Class<T> responseType) {
        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
                }
            }).contentType(MediaType.parseMediaType(
                    image.getContentType() != null ? image.getContentType() : MediaType.IMAGE_JPEG_VALUE
            ));

            return webClientBuilder.build()
                    .post()
                    .uri(aiBaseUrl + path)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(Duration.ofMillis(aiTimeoutMs));
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일을 읽을 수 없습니다.");
        }
    }
}
