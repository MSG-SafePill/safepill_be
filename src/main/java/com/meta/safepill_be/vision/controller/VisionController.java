package com.meta.safepill_be.vision.controller;

import com.meta.safepill_be.vision.dto.PrescriptionOcrResponseDto;
import com.meta.safepill_be.vision.dto.VisionIdentifyResponseDto;
import com.meta.safepill_be.vision.service.VisionAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VisionController {
    private final VisionAnalysisService visionAnalysisService;

    @PostMapping(value = "/vision/identify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VisionIdentifyResponseDto> identifyPill(@RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(visionAnalysisService.identifyPill(image));
    }

    @PostMapping(value = "/ocr/prescription", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrescriptionOcrResponseDto> scanPrescription(@RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(visionAnalysisService.scanPrescription(image));
    }
}
