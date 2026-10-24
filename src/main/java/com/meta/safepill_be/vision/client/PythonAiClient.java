package com.meta.safepill_be.vision.client;

import com.meta.safepill_be.chatbot.dto.AiChatRequestDto;
import com.meta.safepill_be.chatbot.dto.AiChatResponseDto;
import com.meta.safepill_be.medicine.dto.AiInteractionAnalyzeRequestDto;
import com.meta.safepill_be.medicine.dto.AiInteractionAnalyzeResponseDto;
import com.meta.safepill_be.vision.dto.AiMultiPillClassificationResponseDto;
import com.meta.safepill_be.vision.dto.AiPrescriptionOcrResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PythonAiClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${safepill.ai.base-url}")
    private String aiBaseUrl;

    @Value("${safepill.ai.timeout-ms:5000}")
    private long aiTimeoutMs;

    @Value("${safepill.ai.image-connect-timeout-ms:3000}")
    private int imageConnectTimeoutMs;

    @Value("${safepill.ai.image-read-timeout-ms:60000}")
    private long imageReadTimeoutMs;

    @Value("${safepill.ai.image-total-timeout-ms:70000}")
    private long imageTotalTimeoutMs;

    private WebClient imageClient;

    @PostConstruct
    void initializeImageClient() {
        HttpClient httpClient = HttpClient.create()
                .disableRetry(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, imageConnectTimeoutMs)
                .responseTimeout(Duration.ofMillis(imageReadTimeoutMs));
        imageClient = webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public AiMultiPillClassificationResponseDto classifyPills(MultipartFile image) {
        return postImage("/classify-pills", image, AiMultiPillClassificationResponseDto.class);
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
        if (image.isEmpty()) {
            throw new VisionAiException(HttpStatus.BAD_REQUEST, "EMPTY_IMAGE", "이미지를 선택해주세요.");
        }
        if (image.getSize() > 30L * 1024 * 1024) {
            throw new VisionAiException(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_TOO_LARGE", "이미지는 30MB 이하로 선택해주세요.");
        }
        long started = System.nanoTime();
        String requestId = MDC.get("visionRequestId");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        try {
            byte[] imageBytes = image.getBytes();
            log.info("vision request_id={} stage=image_read elapsed_ms={} bytes={}",
                    requestId, (System.nanoTime() - started) / 1_000_000, imageBytes.length);
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("image", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
                }
            }).contentType(MediaType.parseMediaType(
                    image.getContentType() != null ? image.getContentType() : MediaType.IMAGE_JPEG_VALUE
            ));

            T result = imageClient
                    .post()
                    .uri(aiBaseUrl + path)
                    .header("X-Request-ID", requestId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(responseType)
                    .timeout(Duration.ofMillis(imageTotalTimeoutMs))
                    .block();
            if (result == null) {
                throw new VisionAiException(HttpStatus.BAD_GATEWAY, "AI_EMPTY_RESPONSE", "AI 서버에서 결과를 받지 못했습니다.");
            }
            return result;
        } catch (IOException e) {
            throw new VisionAiException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "이미지 파일을 읽을 수 없습니다.");
        } catch (WebClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 413) {
                throw new VisionAiException(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_TOO_LARGE", "이미지는 30MB 이하로 선택해주세요.");
            }
            if (status == 400 || status == 422) {
                throw new VisionAiException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "이미지 형식을 확인한 뒤 다시 시도해주세요.");
            }
            if (status == 503) {
                throw new VisionAiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "AI 서버가 준비 중이거나 다른 이미지를 분석 중입니다. 잠시 후 다시 시도해주세요.");
            }
            throw new VisionAiException(HttpStatus.BAD_GATEWAY, "AI_PROCESSING_FAILED", "이미지 분석 중 오류가 발생했습니다. 다시 시도해주세요.");
        } catch (RuntimeException e) {
            if (hasCause(e, TimeoutException.class) || hasCause(e, ReadTimeoutException.class)) {
                throw new VisionAiException(HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT", "이미지 분석 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            }
            if (e instanceof WebClientRequestException) {
                throw new VisionAiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_CONNECTION_FAILED", "AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
            if (e instanceof VisionAiException) {
                throw e;
            }
            throw new VisionAiException(HttpStatus.BAD_GATEWAY, "AI_INVALID_RESPONSE", "AI 응답을 처리할 수 없습니다. 다시 시도해주세요.");
        } finally {
            log.info("vision request_id={} stage=ai_http_total path={} elapsed_ms={} bytes={}",
                    requestId, path, (System.nanoTime() - started) / 1_000_000, image.getSize());
        }
    }

    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }
}
