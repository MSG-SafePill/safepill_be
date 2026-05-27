package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.repository.IntakeScheduleRepository;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.InteractionRule;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.RiskLevel;
import com.meta.safepill_be.medicine.domain.SupplementIngredient;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.AiInteractionAnalyzeRequestDto;
import com.meta.safepill_be.medicine.dto.AiInteractionAnalyzeResponseDto;
import com.meta.safepill_be.medicine.dto.AiInteractionEvidenceDto;
import com.meta.safepill_be.medicine.dto.AiInteractionIngredientDto;
import com.meta.safepill_be.medicine.dto.AiInteractionItemDto;
import com.meta.safepill_be.medicine.dto.AiInteractionRuleDto;
import com.meta.safepill_be.medicine.dto.AiInteractionWarningDto;
import com.meta.safepill_be.medicine.dto.DurResponseDto;
import com.meta.safepill_be.medicine.dto.InteractionAnalyzeResponseDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.InteractionRuleRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.HealthProfile;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.HealthProfileRepository;
import com.meta.safepill_be.user.repository.UserRepository;
import com.meta.safepill_be.vision.client.PythonAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final InteractionRuleRepository interactionRuleRepository;
    private final IngredientMasterRepository ingredientMasterRepository;
    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final IntakeScheduleRepository intakeScheduleRepository;
    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final PythonAiClient pythonAiClient;
    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.dur-taboo}")
    private String durTabooEndpoint;

    @Transactional
    public void fetchAndSaveInteractionRules() {
        WebClient webClient = createWebClient();
        System.out.println("🚀 병용금기(상극) 데이터 파싱 및 DB 저장을 시작합니다...");
        int totalSaved = 0;
        int page = 1;
        int numOfRows = 500;
        try {
            while (true) {
                final int currentPage = page;
                System.out.println("📄 " + page + "페이지 데이터 요청 중...");
                DurResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(durTabooEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("type", "json")
                                .queryParam("numOfRows", String.valueOf(numOfRows))
                                .queryParam("pageNo", String.valueOf(currentPage))
                                .build())
                        .retrieve()
                        .bodyToMono(DurResponseDto.class)
                        .block();
                if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
                    System.out.println("⚠️ " + page + "페이지에 데이터가 없거나 서버 응답이 이상합니다. 수집을 종료합니다.");
                    break;
                }
                List<DurResponseDto.ItemWrapper> wrappers = response.getBody().getItems();
                int pageCount = 0;
                for (DurResponseDto.ItemWrapper wrapper : wrappers) {
                    DurResponseDto.Item item = wrapper.getItem();
                    if (item == null) continue;
                    String nameA = item.getIngredientA();
                    String nameB = item.getIngredientB();
                    String desc = item.getProhibitContent();
                    if (nameA == null || nameB == null || nameA.isEmpty() || nameB.isEmpty()) continue;
                    IngredientMaster ingredientA = findOrCreateIngredient(nameA);
                    IngredientMaster ingredientB = findOrCreateIngredient(nameB);
                    if (interactionRuleRepository.existsByIngredientA_IdAndIngredientB_Id(
                            ingredientA.getId(), ingredientB.getId())
                            || interactionRuleRepository.existsByIngredientA_IdAndIngredientB_Id(
                            ingredientB.getId(), ingredientA.getId())) {
                        continue;
                    }
                    InteractionRule rule = new InteractionRule();
                    rule.setIngredientA(ingredientA);
                    rule.setIngredientB(ingredientB);
                    rule.setRiskLevel(RiskLevel.DANGER);
                    rule.setDescription(desc != null ? desc : "병용금기 성분입니다.");
                    interactionRuleRepository.save(rule);
                    pageCount++;
                }
                totalSaved += pageCount;
                System.out.println("✅ " + page + "페이지 저장 완료! (누적: " + totalSaved + "건)");
                Integer totalCount = response.getBody().getTotalCount();
                if (totalCount != null && currentPage * numOfRows >= totalCount) {
                    break;
                }
                if (wrappers.size() < numOfRows) {
                    break;
                }
                page++;
                Thread.sleep(100);
            }
            System.out.println("🎉 대규모 상극 데이터 수집 대성공! 총 " + totalSaved + "건 동기화 완료!");
        } catch (Exception e) {
            System.err.println("❌ 상극 데이터 수집 중 에러: " + e.getMessage());
        }
    }

    private IngredientMaster findOrCreateIngredient(String name) {
        return ingredientMasterRepository.findByIngredientName(name)
                .orElseGet(() -> ingredientMasterRepository.save(
                        IngredientMaster.builder()
                                .ingredientName(name)
                                .bestTimeGuide("정보 없음")
                                .intakeTip("정보 없음")
                                .build()
                ));
    }

    private WebClient createWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return WebClient.builder().uriBuilderFactory(factory).build();
    }

    @Transactional(readOnly = true)
    public List<InteractionAnalyzeResponseDto> analyzeInteractions(List<Long> medicineIds) {
        List<InteractionAnalyzeResponseDto> result = new java.util.ArrayList<>();
        List<com.meta.safepill_be.medicine.domain.MedicineMaster> medicines = medicineMasterRepository.findAllById(medicineIds);
        java.util.Map<Long, java.util.Set<com.meta.safepill_be.medicine.domain.MedicineMaster>> ingredientToMedicinesMap = new java.util.HashMap<>();
        java.util.Set<Long> allIngredientIds = new java.util.HashSet<>();
        for (com.meta.safepill_be.medicine.domain.MedicineMaster medicine : medicines) {
            for (com.meta.safepill_be.medicine.domain.MedicineIngredient mi : medicine.getIngredients()) {
                Long ingId = mi.getIngredientMaster().getId();
                allIngredientIds.add(ingId);
                ingredientToMedicinesMap.computeIfAbsent(ingId, k -> new java.util.HashSet<>()).add(medicine);
            }
        }
        if (allIngredientIds.isEmpty()) return result;
        List<InteractionRule> triggeredRules = interactionRuleRepository.findInteractionsByIngredientIds(new java.util.ArrayList<>(allIngredientIds));
        java.util.Set<String> alreadyAddedPairs = new java.util.HashSet<>();
        for (InteractionRule rule : triggeredRules) {
            Long ingAId = rule.getIngredientA().getId();
            Long ingBId = rule.getIngredientB().getId();
            java.util.Set<com.meta.safepill_be.medicine.domain.MedicineMaster> medsContainingA = ingredientToMedicinesMap.get(ingAId);
            java.util.Set<com.meta.safepill_be.medicine.domain.MedicineMaster> medsContainingB = ingredientToMedicinesMap.get(ingBId);
            if (medsContainingA == null || medsContainingB == null) continue;
            for (com.meta.safepill_be.medicine.domain.MedicineMaster medA : medsContainingA) {
                for (com.meta.safepill_be.medicine.domain.MedicineMaster medB : medsContainingB) {
                    if (!medA.getId().equals(medB.getId())) {
                        Long minId = Math.min(medA.getId(), medB.getId());
                        Long maxId = Math.max(medA.getId(), medB.getId());
                        String pairKey = minId + "_" + maxId;
                        if (!alreadyAddedPairs.contains(pairKey)) {
                            alreadyAddedPairs.add(pairKey); // 명부에 등록
                            result.add(InteractionAnalyzeResponseDto.builder()
                                    .medicineNameA(medA.getMedicineName())
                                    .medicineNameB(medB.getMedicineName())
                                    .ingredientNameA(rule.getIngredientA().getIngredientName())
                                    .ingredientNameB(rule.getIngredientB().getIngredientName())
                                    .riskLevel(rule.getRiskLevel())
                                    .description(rule.getDescription())
                                    .build());
                        }
                    }
                }
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<InteractionAnalyzeResponseDto> analyzeMyCabinetInteractions(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(user.getId());
        if (registrations.size() < 2) {
            return List.of();
        }

        List<CabinetIngredientSource> sources = buildCabinetIngredientSources(registrations);
        Map<Long, Set<CabinetIngredientSource>> ingredientToSources = new HashMap<>();
        for (CabinetIngredientSource source : sources) {
            ingredientToSources.computeIfAbsent(source.ingredientId(), key -> new HashSet<>()).add(source);
        }

        if (ingredientToSources.size() < 2) {
            return List.of();
        }

        List<InteractionRule> triggeredRules = interactionRuleRepository.findInteractionsByIngredientIds(
                new ArrayList<>(ingredientToSources.keySet()));
        List<InteractionAnalyzeResponseDto> result = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();

        for (InteractionRule rule : triggeredRules) {
            Set<CabinetIngredientSource> sourcesA = ingredientToSources.get(rule.getIngredientA().getId());
            Set<CabinetIngredientSource> sourcesB = ingredientToSources.get(rule.getIngredientB().getId());
            if (sourcesA == null || sourcesB == null) {
                continue;
            }

            for (CabinetIngredientSource sourceA : sourcesA) {
                for (CabinetIngredientSource sourceB : sourcesB) {
                    if (sourceA.regId().equals(sourceB.regId())) {
                        continue;
                    }

                    Long minRegId = Math.min(sourceA.regId(), sourceB.regId());
                    Long maxRegId = Math.max(sourceA.regId(), sourceB.regId());
                    Long minIngredientId = Math.min(rule.getIngredientA().getId(), rule.getIngredientB().getId());
                    Long maxIngredientId = Math.max(rule.getIngredientA().getId(), rule.getIngredientB().getId());
                    String key = minRegId + "_" + maxRegId + "_" + minIngredientId + "_" + maxIngredientId;
                    if (!alreadyAdded.add(key)) {
                        continue;
                    }

                    result.add(InteractionAnalyzeResponseDto.builder()
                            .itemNameA(sourceA.itemName())
                            .itemNameB(sourceB.itemName())
                            .itemTypeA(sourceA.itemType())
                            .itemTypeB(sourceB.itemType())
                            .medicineNameA(sourceA.itemName())
                            .medicineNameB(sourceB.itemName())
                            .ingredientNameA(rule.getIngredientA().getIngredientName())
                            .ingredientNameB(rule.getIngredientB().getIngredientName())
                            .riskLevel(rule.getRiskLevel())
                            .description(rule.getDescription())
                            .build());
                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public AiInteractionAnalyzeResponseDto analyzeMyCabinetInteractionsWithAi(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(user.getId());
        Map<Long, List<String>> intakeTimesByRegId = buildIntakeTimesByRegId(user.getId());
        List<AiInteractionItemDto> items = buildAiInteractionItems(registrations, intakeTimesByRegId);
        List<AiInteractionRuleDto> rules = analyzeMyCabinetInteractions(loginId).stream()
                .map(result -> AiInteractionRuleDto.builder()
                        .itemNameA(result.getItemNameA())
                        .itemNameB(result.getItemNameB())
                        .ingredientNameA(result.getIngredientNameA())
                        .ingredientNameB(result.getIngredientNameB())
                        .riskLevel(result.getRiskLevel() != null ? result.getRiskLevel().name() : null)
                        .description(result.getDescription())
                        .build())
                .collect(Collectors.toList());

        AiInteractionAnalyzeRequestDto requestDto = AiInteractionAnalyzeRequestDto.builder()
                .items(items)
                .interactionRules(rules)
                .userProfile(buildUserProfile(user))
                .build();

        try {
            AiInteractionAnalyzeResponseDto response = pythonAiClient.analyzeInteraction(requestDto);
            if (response == null) {
                return buildFallbackAiAnalysis(items, rules, "AI 서버가 빈 응답을 반환했습니다.");
            }
            return response;
        } catch (RuntimeException e) {
            return buildFallbackAiAnalysis(items, rules, "AI 서버 연결에 실패하여 DUR 룰 기반으로 분석했습니다.");
        }
    }

    private List<CabinetIngredientSource> buildCabinetIngredientSources(List<UserMedicationReg> registrations) {
        Map<Long, UserMedicationReg> medicineRegsByItemId = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.MEDICINE)
                .collect(Collectors.toMap(UserMedicationReg::getItemId, Function.identity(), (left, right) -> left));
        Map<Long, UserMedicationReg> supplementRegsByItemId = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.SUPPLEMENT)
                .collect(Collectors.toMap(UserMedicationReg::getItemId, Function.identity(), (left, right) -> left));

        List<CabinetIngredientSource> sources = new ArrayList<>();
        List<MedicineMaster> medicines = medicineRegsByItemId.isEmpty()
                ? List.of()
                : medicineMasterRepository.findByIdIn(new ArrayList<>(medicineRegsByItemId.keySet()));
        for (MedicineMaster medicine : medicines) {
            UserMedicationReg reg = medicineRegsByItemId.get(medicine.getId());
            if (reg == null) {
                continue;
            }
            for (MedicineIngredient ingredient : medicine.getIngredients()) {
                sources.add(new CabinetIngredientSource(
                        reg.getId(),
                        ItemType.MEDICINE,
                        medicine.getMedicineName(),
                        ingredient.getIngredientMaster().getId()));
            }
        }

        List<SupplementMaster> supplements = supplementRegsByItemId.isEmpty()
                ? List.of()
                : supplementMasterRepository.findByIdIn(new ArrayList<>(supplementRegsByItemId.keySet()));
        for (SupplementMaster supplement : supplements) {
            UserMedicationReg reg = supplementRegsByItemId.get(supplement.getId());
            if (reg == null) {
                continue;
            }
            for (SupplementIngredient ingredient : supplement.getIngredients()) {
                sources.add(new CabinetIngredientSource(
                        reg.getId(),
                        ItemType.SUPPLEMENT,
                        supplement.getSupplementName(),
                        ingredient.getIngredientMaster().getId()));
            }
        }

        return sources;
    }

    private List<AiInteractionItemDto> buildAiInteractionItems(
            List<UserMedicationReg> registrations,
            Map<Long, List<String>> intakeTimesByRegId
    ) {
        Map<Long, UserMedicationReg> medicineRegsByItemId = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.MEDICINE)
                .collect(Collectors.toMap(UserMedicationReg::getItemId, Function.identity(), (left, right) -> left));
        Map<Long, UserMedicationReg> supplementRegsByItemId = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.SUPPLEMENT)
                .collect(Collectors.toMap(UserMedicationReg::getItemId, Function.identity(), (left, right) -> left));

        List<AiInteractionItemDto> items = new ArrayList<>();
        List<MedicineMaster> medicines = medicineRegsByItemId.isEmpty()
                ? List.of()
                : medicineMasterRepository.findByIdIn(new ArrayList<>(medicineRegsByItemId.keySet()));
        for (MedicineMaster medicine : medicines) {
            items.add(AiInteractionItemDto.builder()
                    .itemName(medicine.getMedicineName())
                    .itemType(ItemType.MEDICINE.name())
                    .ingredients(medicine.getIngredients().stream()
                            .map(ingredient -> AiInteractionIngredientDto.builder()
                                    .name(ingredient.getIngredientMaster().getIngredientName())
                                    .dosage(ingredient.getDosage() != null ? ingredient.getDosage().toPlainString() : null)
                                    .build())
                            .collect(Collectors.toList()))
                    .intakeTimes(intakeTimesByRegId.getOrDefault(medicineRegsByItemId.get(medicine.getId()).getId(), List.of()))
                    .efficacy(medicine.getEfficacy())
                    .precautions(medicine.getPrecautions())
                    .build());
        }

        List<SupplementMaster> supplements = supplementRegsByItemId.isEmpty()
                ? List.of()
                : supplementMasterRepository.findByIdIn(new ArrayList<>(supplementRegsByItemId.keySet()));
        for (SupplementMaster supplement : supplements) {
            items.add(AiInteractionItemDto.builder()
                    .itemName(supplement.getSupplementName())
                    .itemType(ItemType.SUPPLEMENT.name())
                    .ingredients(supplement.getIngredients().stream()
                            .map(ingredient -> AiInteractionIngredientDto.builder()
                                    .name(ingredient.getIngredientMaster().getIngredientName())
                                    .dosage(ingredient.getDosage() != null ? ingredient.getDosage().toPlainString() : null)
                                    .build())
                            .collect(Collectors.toList()))
                    .intakeTimes(intakeTimesByRegId.getOrDefault(supplementRegsByItemId.get(supplement.getId()).getId(), List.of()))
                    .efficacy(supplement.getEfficacy())
                    .precautions(supplement.getPrecautions())
                    .build());
        }

        return items;
    }

    private Map<Long, List<String>> buildIntakeTimesByRegId(Long userId) {
        return intakeScheduleRepository.findSchedulesForUser(userId).stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getUserMedicationReg().getId(),
                        Collectors.mapping(schedule -> schedule.getTimeSlot(), Collectors.toList())
                ));
    }

    private Map<String, Object> buildUserProfile(User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("gender", user.getGender() != null ? user.getGender().name() : null);
        profile.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null);
        healthProfileRepository.findByUserId(user.getId()).ifPresent(healthProfile -> addHealthProfile(profile, healthProfile));
        return profile;
    }

    private void addHealthProfile(Map<String, Object> profile, HealthProfile healthProfile) {
        profile.put("disease", healthProfile.getDisease());
        profile.put("allergy", healthProfile.getAllergy());
    }

    private AiInteractionAnalyzeResponseDto buildFallbackAiAnalysis(
            List<AiInteractionItemDto> items,
            List<AiInteractionRuleDto> rules,
            String fallbackReason
    ) {
        if (items.size() < 2) {
            return AiInteractionAnalyzeResponseDto.builder()
                    .requestId("fallback-" + UUID.randomUUID())
                    .status("fallback")
                    .riskLevel("NONE")
                    .summary("분석할 약품 또는 영양제가 2개 미만입니다.")
                    .warnings(List.of())
                    .recommendations(List.of("새로운 약이나 영양제를 추가하기 전에는 의사 또는 약사와 상담하세요."))
                    .scheduleRecommendations(List.of())
                    .foodWarnings(List.of())
                    .consultationGuidance(List.of("새 약을 추가하거나 처방이 변경되면 의사 또는 약사에게 현재 복용 목록을 보여주세요."))
                    .evidence(List.of(AiInteractionEvidenceDto.builder()
                            .source("BACKEND_FALLBACK")
                            .text(fallbackReason)
                            .build()))
                    .disclaimer(disclaimer())
                    .build();
        }

        if (rules.isEmpty()) {
            return AiInteractionAnalyzeResponseDto.builder()
                    .requestId("fallback-" + UUID.randomUUID())
                    .status("fallback")
                    .riskLevel("NONE")
                    .summary("현재 등록된 DUR 룰 기준으로 확인된 병용금기 또는 주의 상호작용은 없습니다.")
                    .warnings(List.of())
                    .recommendations(List.of(
                            "새로운 약이나 영양제를 추가하기 전에는 현재 복용 목록을 의사 또는 약사에게 보여주세요.",
                            "증상 변화가 있거나 여러 약을 장기간 함께 복용한다면 전문가 검토가 필요합니다."
                    ))
                    .scheduleRecommendations(buildScheduleRecommendations(items, rules))
                    .foodWarnings(List.of("현재 데이터 기준으로 명확한 음식 상호작용은 확인되지 않았습니다. 술이나 새 영양제를 추가할 때는 전문가에게 확인하세요."))
                    .consultationGuidance(List.of("임신, 수유, 신장/간 질환, 심한 알레르기 병력이 있으면 복용 전 상담이 필요합니다."))
                    .evidence(List.of(AiInteractionEvidenceDto.builder()
                            .source("BACKEND_FALLBACK")
                            .text(fallbackReason)
                            .build()))
                    .disclaimer(disclaimer())
                    .build();
        }

        List<AiInteractionWarningDto> warnings = rules.stream()
                .map(rule -> AiInteractionWarningDto.builder()
                        .title(rule.getIngredientNameA() + " + " + rule.getIngredientNameB() + " 병용 주의")
                        .severity(normalizeRiskLevel(rule.getRiskLevel()))
                        .items(List.of(
                                rule.getItemNameA() != null ? rule.getItemNameA() : "",
                                rule.getItemNameB() != null ? rule.getItemNameB() : ""
                        ).stream().filter(value -> !value.isBlank()).toList())
                        .reason(rule.getDescription() != null ? rule.getDescription() : "상호작용 가능성이 있어 주의가 필요합니다.")
                        .build())
                .toList();
        List<AiInteractionEvidenceDto> evidence = new ArrayList<>();
        evidence.add(AiInteractionEvidenceDto.builder()
                .source("BACKEND_FALLBACK")
                .text(fallbackReason)
                .build());
        evidence.addAll(rules.stream()
                .map(rule -> AiInteractionEvidenceDto.builder()
                        .source("DUR_RULE")
                        .text(rule.getDescription() != null ? rule.getDescription() : "DUR 상호작용 룰")
                        .build())
                .toList());

        return AiInteractionAnalyzeResponseDto.builder()
                .requestId("fallback-" + UUID.randomUUID())
                .status("fallback")
                .riskLevel(highestRiskLevel(rules))
                .summary("총 " + items.size() + "개 항목에서 " + rules.size() + "건의 상호작용 주의 항목이 확인되었습니다.")
                .warnings(warnings)
                .recommendations(List.of(
                        "복용을 임의로 중단하거나 용량을 바꾸지 말고 의사 또는 약사와 상담하세요.",
                        "같은 시간대에 함께 복용 중이라면 상담 전까지 복용 시간 조정이 필요한지 확인하세요.",
                        "출혈, 호흡곤란, 심한 발진, 의식 저하 같은 증상이 있으면 즉시 의료기관을 방문하세요."
                ))
                .scheduleRecommendations(buildScheduleRecommendations(items, rules))
                .foodWarnings(List.of(
                        "상호작용 위험이 확인된 조합은 술이나 새 영양제를 함께 추가하지 말고 전문가에게 먼저 확인하세요.",
                        "철분, 칼슘, 마그네슘 같은 미네랄은 일부 약의 흡수를 방해할 수 있어 복용 간격 확인이 필요합니다."
                ))
                .consultationGuidance(List.of(
                        "DANGER 또는 WARNING 조합은 의사 또는 약사 상담 전까지 임의로 같이 복용하지 마세요.",
                        "출혈, 호흡곤란, 심한 발진, 실신, 의식 저하 같은 증상이 있으면 즉시 의료기관을 방문하세요."
                ))
                .evidence(evidence)
                .disclaimer(disclaimer())
                .build();
    }

    private List<String> buildScheduleRecommendations(List<AiInteractionItemDto> items, List<AiInteractionRuleDto> rules) {
        List<String> recommendations = new ArrayList<>();
        boolean hasAnySchedule = items.stream()
                .anyMatch(item -> item.getIntakeTimes() != null && !item.getIntakeTimes().isEmpty());
        if (!hasAnySchedule) {
            recommendations.add("등록된 복용 시간이 없습니다. 복용 시간을 등록하면 같은 시간대 병용 여부를 더 정확히 확인할 수 있습니다.");
            return recommendations;
        }

        recommendations.add("복용 시간이 같은 약이나 영양제는 상호작용 경고가 있는지 먼저 확인하세요.");
        if (!rules.isEmpty()) {
            recommendations.add("상호작용 주의 조합은 같은 시간대 복용 여부를 의사 또는 약사에게 확인한 뒤 조정하세요.");
        }
        recommendations.add("처방전에서 식전/식후 지시가 있는 약은 해당 지시를 우선하고, 임의로 시간을 바꾸지 마세요.");
        return recommendations;
    }

    private String highestRiskLevel(List<AiInteractionRuleDto> rules) {
        int maxScore = 0;
        String maxRisk = "NONE";
        for (AiInteractionRuleDto rule : rules) {
            String risk = normalizeRiskLevel(rule.getRiskLevel());
            int score = riskScore(risk);
            if (score > maxScore) {
                maxScore = score;
                maxRisk = risk;
            }
        }
        return maxRisk;
    }

    private String normalizeRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            return "CAUTION";
        }
        String normalized = riskLevel.trim().toUpperCase();
        if (!List.of("CAUTION", "WARNING", "DANGER").contains(normalized)) {
            return "CAUTION";
        }
        return normalized;
    }

    private int riskScore(String riskLevel) {
        return switch (riskLevel) {
            case "DANGER" -> 3;
            case "WARNING" -> 2;
            case "CAUTION" -> 1;
            default -> 0;
        };
    }

    private String disclaimer() {
        return "이 분석은 참고용이며 진단이나 처방이 아닙니다. 복용 변경 전 의사 또는 약사와 상담하세요.";
    }

    private record CabinetIngredientSource(
            Long regId,
            ItemType itemType,
            String itemName,
            Long ingredientId
    ) {
    }
}
