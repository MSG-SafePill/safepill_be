package com.meta.safepill_be.vision.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class VisionTimingFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/api/vision/identify") && !path.equals("/api/ocr/prescription");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
        long started = System.nanoTime();
        String requestId = UUID.randomUUID().toString();
        response.setHeader("X-Request-ID", requestId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("visionRequestId", requestId)) {
            chain.doFilter(request, response);
        } finally {
            log.info("vision request_id={} stage=backend_total path={} status={} elapsed_ms={} content_length={}",
                    requestId, request.getServletPath(), response.getStatus(),
                    (System.nanoTime() - started) / 1_000_000, request.getContentLengthLong());
        }
    }
}
