package com.meta.safepill_be.vision.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

// No Spring application context or DB is started by these tests.
class PythonAiClientTest {
    private final MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});

    private PythonAiClient client(ExchangeFunction exchange) {
        var client = new PythonAiClient(WebClient.builder().exchangeFunction(exchange));
        ReflectionTestUtils.setField(client, "aiBaseUrl", "http://localhost:8000");
        ReflectionTestUtils.setField(client, "imageConnectTimeoutMs", 100);
        ReflectionTestUtils.setField(client, "imageReadTimeoutMs", 100);
        ReflectionTestUtils.setField(client, "imageTotalTimeoutMs", 500L);
        client.initializeImageClient();
        return client;
    }

    @Test
    void successfulResponsePreservesContractAndTraceHeader() {
        var client = client(request -> {
            assertEquals("/classify-pills", request.url().getPath());
            assertNotNull(request.headers().getFirst("X-Request-ID"));
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"success\":false,\"detectedCount\":0,\"detectedPills\":[]}").build());
        });
        var response = client.classifyPills(image);
        assertFalse(response.isSuccess());
        assertEquals(0, response.getDetectedCount());
        assertTrue(response.getDetectedPills().isEmpty());
    }

    @Test
    void hangingResponseProducesTimeoutError() {
        var client = client(request -> Mono.never());
        ReflectionTestUtils.setField(client, "imageTotalTimeoutMs", 20L);
        var error = assertThrows(VisionAiException.class, () -> client.classifyPills(image));
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, error.getStatus());
        assertEquals("AI_TIMEOUT", error.getCode());
    }

    @Test
    void connectionFailureProducesUnavailableError() {
        var client = client(request -> Mono.error(new WebClientRequestException(
                new ConnectException("refused"), HttpMethod.POST, URI.create("http://localhost:8000"), new HttpHeaders())));
        var error = assertThrows(VisionAiException.class, () -> client.classifyPills(image));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatus());
        assertEquals("AI_CONNECTION_FAILED", error.getCode());
    }

    @Test
    void upstreamBusyAndInvalidImageAreNotGenericServerErrors() {
        for (var status : new HttpStatus[]{HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.BAD_REQUEST, HttpStatus.PAYLOAD_TOO_LARGE}) {
            var client = client(request -> Mono.just(ClientResponse.create(status).body("failure").build()));
            var error = assertThrows(VisionAiException.class, () -> client.classifyPills(image));
            assertEquals(status, error.getStatus());
        }
    }

    @Test
    void emptyImageDoesNotCallAiServer() {
        var client = client(request -> { fail("must not send empty images"); return Mono.empty(); });
        var error = assertThrows(VisionAiException.class,
                () -> client.classifyPills(new MockMultipartFile("image", new byte[0])));
        assertEquals("EMPTY_IMAGE", error.getCode());
    }
}
