package com.meta.safepill_be.vision.service;

import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.InteractionRule;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.repository.InteractionRuleRepository;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.MedicineIngredientRepository;
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
    private final IngredientMasterRepository ingredientMasterRepository;
    private final MedicineIngredientRepository medicineIngredientRepository;
    private final InteractionRuleRepository interactionRuleRepository;

    @Transactional(readOnly = true)
    public VisionIdentifyResponseDto identifyPill(MultipartFile image) {
        AiMultiPillClassificationResponseDto aiResponse = pythonAiClient.classifyPills(image);
        List<VisionMedicineCandidateDto> candidates = new ArrayList<>();
        if (aiResponse != null && aiResponse.getDetectedPills() != null) {
            for (AiDetectedPillClassificationDto detectedPill : aiResponse.getDetectedPills()) {
                if (detectedPill.getCandidates() == null || detectedPill.getCandidates().isEmpty()) {
                    continue;
                }
                AiPillClassificationCandidateDto item = detectedPill.getCandidates().get(0);
                List<VisionMedicineCandidateDto> matched = matchMedicines(
                        item.getMedicineName(),
                        item.getScore(),
                        item.getClassLabel()
                );
                candidates.add(matched.isEmpty() ? toClassificationCandidate(item) : matched.get(0));
            }
        }
        return VisionIdentifyResponseDto.builder()
                .status(candidates.isEmpty() ? "no_match" : "ok")
                .candidates(candidates)
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
        if (medicines.isEmpty()) {
            medicines = matchMedicinesByIngredient(keyword);
        }

        return medicines.stream()
                .map(medicine -> toCandidate(medicine, confidence, matchedText))
                .toList();
    }

    private List<MedicineMaster> matchMedicinesByIngredient(String normalizedKeyword) {
        List<String> ingredientHints = extractIngredientHints(normalizedKeyword);
        Map<Long, MedicineMaster> medicinesById = new LinkedHashMap<>();
        for (String hint : ingredientHints) {
            if (hint.length() < 2) {
                continue;
            }
            List<IngredientMaster> ingredients = ingredientMasterRepository.findTop5ByIngredientNameContainingIgnoreCase(hint);
            for (IngredientMaster ingredient : ingredients) {
                List<MedicineIngredient> medicineIngredients = medicineIngredientRepository.findTop10ByIngredientMaster_Id(ingredient.getId());
                for (MedicineIngredient medicineIngredient : medicineIngredients) {
                    MedicineMaster medicine = medicineIngredient.getMedicineMaster();
                    medicinesById.putIfAbsent(medicine.getId(), medicine);
                }
            }
        }
        return new ArrayList<>(medicinesById.values());
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

    private VisionMedicineCandidateDto toClassificationCandidate(AiPillClassificationCandidateDto item) {
        return VisionMedicineCandidateDto.builder()
                .itemSeq(item.getClassLabel())
                .medicineName(item.getMedicineName())
                .manufacturer(item.getManufacturer())
                .confidence(item.getScore())
                .matchedText(item.getReason())
                .ingredients(List.of())
                .interactionWarnings(List.of())
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
        Map<String, VisionMedicineCandidateDto> bestById = new LinkedHashMap<>();
        for (VisionMedicineCandidateDto candidate : candidates) {
            String key = candidate.getMedicineId() != null
                    ? "id:" + candidate.getMedicineId()
                    : "name:" + candidate.getMedicineName();
            VisionMedicineCandidateDto previous = bestById.get(key);
            if (previous == null || candidate.getConfidence() > previous.getConfidence()) {
                bestById.put(key, candidate);
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

    private List<String> extractIngredientHints(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        List<String> hints = new ArrayList<>();
        hints.add(keyword);
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\(([^)]{2,})\\)").matcher(keyword);
        while (matcher.find()) {
            hints.add(normalizeKeyword(matcher.group(1)));
        }
        String withoutDose = keyword.replaceAll("\\d+(?:\\.\\d+)?(?:MG|G|MCG|UG|ML|밀리그램)", "");
        hints.add(withoutDose);
        return hints.stream()
                .map(this::normalizeKeyword)
                .filter(hint -> !hint.isBlank())
                .distinct()
                .toList();
    }
}
