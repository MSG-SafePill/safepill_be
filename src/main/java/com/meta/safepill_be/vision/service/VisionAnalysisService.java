package com.meta.safepill_be.vision.service;

import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.InteractionRule;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.repository.InteractionRuleRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.vision.client.PythonAiClient;
import com.meta.safepill_be.vision.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisionAnalysisService {
    private final PythonAiClient pythonAiClient;
    private final MedicineMasterRepository medicineMasterRepository;
    private final InteractionRuleRepository interactionRuleRepository;

    @Transactional(readOnly = true)
    public VisionIdentifyResponseDto identifyPill(MultipartFile image) {
        AiIdentifyResponseDto aiResponse = pythonAiClient.identifyPill(image);
        List<VisionMedicineCandidateDto> candidates = new ArrayList<>();
        if (aiResponse != null && aiResponse.getIdentifiedPills() != null) {
            for (AiIdentifiedPillDto item : aiResponse.getIdentifiedPills()) {
                candidates.addAll(matchMedicines(item.getPillName(), item.getConfidence(), item.getMatchedText()));
            }
        }
        return VisionIdentifyResponseDto.builder()
                .requestId(aiResponse != null ? aiResponse.getRequestId() : null)
                .status(candidates.isEmpty() ? "no_match" : "ok")
                .candidates(deduplicateByMedicineId(candidates))
                .build();
    }

    @Transactional(readOnly = true)
    public PrescriptionOcrResponseDto scanPrescription(MultipartFile image) {
        AiPrescriptionOcrResponseDto aiResponse = pythonAiClient.scanPrescription(image);
        List<PrescriptionOcrItemResponseDto> items = new ArrayList<>();
        if (aiResponse != null && aiResponse.getItems() != null) {
            for (AiPrescriptionOcrItemDto item : aiResponse.getItems()) {
                List<VisionMedicineCandidateDto> candidates = matchMedicines(
                        item.getMedicineName(),
                        item.getConfidence(),
                        item.getRawText()
                );
                VisionMedicineCandidateDto best = candidates.isEmpty() ? null : candidates.get(0);
                items.add(PrescriptionOcrItemResponseDto.builder()
                        .rawText(item.getRawText())
                        .medicineName(item.getMedicineName())
                        .dosage(item.getDosage())
                        .frequency(item.getFrequency())
                        .mealTiming(item.getMealTiming())
                        .days(item.getDays())
                        .confidence(item.getConfidence())
                        .matchedMedicineId(best != null ? best.getMedicineId() : null)
                        .matchedMedicineName(best != null ? best.getMedicineName() : null)
                        .candidates(candidates)
                        .build());
            }
        }
        return PrescriptionOcrResponseDto.builder()
                .requestId(aiResponse != null ? aiResponse.getRequestId() : null)
                .status(items.isEmpty() ? "no_match" : "ok")
                .items(items)
                .build();
    }

    private List<VisionMedicineCandidateDto> matchMedicines(String keyword, double confidence, String matchedText) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }

        List<MedicineMaster> medicines = medicineMasterRepository.findTop5ByMedicineNameContainingIgnoreCase(normalizedKeyword);
        if (medicines.isEmpty() && normalizedKeyword.length() > 4) {
            medicines = medicineMasterRepository.findTop5ByMedicineNameContainingIgnoreCase(normalizedKeyword.substring(0, 4));
        }

        return medicines.stream()
                .map(medicine -> toCandidate(medicine, confidence, matchedText))
                .toList();
    }

    private VisionMedicineCandidateDto toCandidate(MedicineMaster medicine, double confidence, String matchedText) {
        return VisionMedicineCandidateDto.builder()
                .medicineId(medicine.getId())
                .itemSeq(medicine.getItemSeq())
                .medicineName(medicine.getMedicineName())
                .manufacturer(medicine.getMedicineManufacturer())
                .imageUrl(medicine.getAppearanceInfo() != null ? medicine.getAppearanceInfo().getImageUrl() : null)
                .efficacy(medicine.getEfficacy())
                .useMethod(medicine.getUseMethod())
                .precautions(medicine.getPrecautions())
                .confidence(confidence)
                .matchedText(matchedText)
                .ingredients(toIngredients(medicine))
                .interactionWarnings(toInteractionWarnings(medicine))
                .build();
    }

    private List<VisionIngredientDto> toIngredients(MedicineMaster medicine) {
        return medicine.getIngredients().stream()
                .map(medicineIngredient -> {
                    IngredientMaster ingredient = medicineIngredient.getIngredientMaster();
                    return VisionIngredientDto.builder()
                            .id(ingredient.getId())
                            .name(ingredient.getIngredientName())
                            .dosage(medicineIngredient.getDosage())
                            .unit(ingredient.getUnit())
                            .build();
                })
                .toList();
    }

    private List<VisionInteractionWarningDto> toInteractionWarnings(MedicineMaster medicine) {
        List<VisionInteractionWarningDto> warnings = new ArrayList<>();
        for (MedicineIngredient medicineIngredient : medicine.getIngredients()) {
            IngredientMaster source = medicineIngredient.getIngredientMaster();
            List<InteractionRule> rules = interactionRuleRepository.findByIngredientA_IdOrIngredientB_Id(source.getId(), source.getId());
            for (InteractionRule rule : rules) {
                IngredientMaster target = rule.getIngredientA().getId().equals(source.getId())
                        ? rule.getIngredientB()
                        : rule.getIngredientA();
                warnings.add(VisionInteractionWarningDto.builder()
                        .sourceIngredient(source.getIngredientName())
                        .targetIngredient(target.getIngredientName())
                        .riskLevel(rule.getRiskLevel())
                        .description(rule.getDescription())
                        .build());
            }
        }
        return warnings;
    }

    private List<VisionMedicineCandidateDto> deduplicateByMedicineId(List<VisionMedicineCandidateDto> candidates) {
        Map<Long, VisionMedicineCandidateDto> bestById = new LinkedHashMap<>();
        for (VisionMedicineCandidateDto candidate : candidates) {
            VisionMedicineCandidateDto previous = bestById.get(candidate.getMedicineId());
            if (previous == null || candidate.getConfidence() > previous.getConfidence()) {
                bestById.put(candidate.getMedicineId(), candidate);
            }
        }
        return new ArrayList<>(bestById.values());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.replaceAll("\\([^)]*\\)", "")
                .replaceAll("\\[[^]]*]", "")
                .replaceAll("[^0-9A-Za-z가-힣]", "")
                .trim();
    }
}
